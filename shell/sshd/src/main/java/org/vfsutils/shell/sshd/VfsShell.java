package org.vfsutils.shell.sshd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.FileSystemAware;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.boxed.BoxedCommandRegistry;
import org.vfsutils.shell.boxed.BoxedEngine;
import org.vfsutils.shell.commands.Register;
import org.vfsutils.shell.commands.Unregister;
import org.vfsutils.shell.sshd.filesystem.VfsSshFile;
import org.vfsutils.shell.sshd.filesystem.VfsFileSystemView;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

public class VfsShell implements Command, ConsoleInterface, Runnable, SessionAware, FileSystemAware {
	
	protected static final Logger log = LoggerFactory.getLogger(VfsShell.class);

	private PrintStream out = null;
	private PrintStream err = null;
	private Reader in = null;
	
	private ExitCallback callback;
	private Thread thread;
	
	private BoxedEngine engine;
	
	private VfsFileSystemView root;
		
	public VfsShell() {
	}

	public void destroy() {
		thread.interrupt();
	}

	public void setErrorStream(OutputStream err) {
		this.err = new PrintStream(err);
	}

	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	public void setInputStream(InputStream in) {
		this.in = new InputStreamReader(in);
	}

	public void setOutputStream(OutputStream out) {
		this.out = new PrintStream(out);
	}

	public void start(Environment env) throws IOException {
		
		String term = env.getEnv().get("TERM");
		log.debug("Client terminal: " + term);
		
		if (term != null && (term.toLowerCase().startsWith("xterm") || term.toLowerCase().equals("cygwin"))) {
			this.in = new EchoReader(this.in, this);
			log.debug("Activated echo");
		}
				
		thread = new Thread(this, "VfsShell");
        thread.start();
		
	}
	
	


	//ConsoleInterface
	
	
	public void error(Object arg0) {
		err.print(arg0);
		err.println('\r');
		err.flush();
	}

	public PrintStream getErr() {
		return err;
	}

	public Reader getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}

	public void print(Object arg0) {
		out.print(arg0);	
		out.flush();
	}

	public void println(Object arg0) {
		out.print(arg0);
		out.println('\r');
		out.flush();
	}
	
	//Runnable

	public void run() {
		
		try {
			FileObject rootFile = ((VfsSshFile) this.root.getFile(".")).getVfsFile();
			engine = new BoxedEngine(this, new BoxedCommandRegistry(), rootFile.getFileSystem().getFileSystemManager());	
			
			//userDir is where the server is started
			FileObject userDir = engine.getCwd();
			
			customizeEngine(engine);
			
			loadGlobalRc(engine, userDir);
			
			//set to VFS start dir
			engine.setStartDir(rootFile);
			
			loadUserRc(engine, rootFile);
			
			//engine.setEchoOn(true);
			engine.go();
		} catch (Exception e) {
			log.warn("Exception in shell", e);
			getErr().println(e.getMessage());
		}
		finally {
			callback.onExit(0);
		}
	}

	public void setSession(ServerSession session) {
	}
	
	
	public void setFileSystemView(FileSystemView view) {
		log.info("Setting filesystem");
		if (view instanceof VfsFileSystemView) {
			this.root = (VfsFileSystemView) view;
		}
		else {
			//what to do?
			log.error("VfsShell only supports the VfsFileSystemView");
		}
	}

	/**
	 * Customizes the engine using a script
	 * @param engine
	 */
	protected void customizeEngine(Engine engine) {
		
		String configScriptLocation = System.getProperty("org.dctmvfs.vfs.server.customscript", "vfs-server-custom-init.bsh");

		//escape mode
		if (configScriptLocation.equals("none")) {
			return;
		}
		
		try {
			// get the classloader (pattern copied from VFS)
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader == null) {
				classLoader = getClass().getClassLoader();
			}
			
			URL configScriptUrl = classLoader.getResource(configScriptLocation);				
			if (configScriptUrl!=null) {
				//eval script
			
				//log.debug("Loading " + scriptType + " configuration");
				
				// create interpreter and set context
				Interpreter bsh = new Interpreter();
				bsh.set("engine", engine);
				
				Reader reader = new InputStreamReader(configScriptUrl.openStream());
				try {
					bsh.eval(reader);
				}
				finally {
					reader.close();
				}
				//log.info("Loaded " + configScriptLocation);
				
			}
		} catch (FileNotFoundException e) {
			this.error("Could not find configuration script on location " + configScriptLocation + ": " + e.getMessage());
		} catch (EvalError e) {
			this.error("Error executing configuration script " + configScriptLocation + ": " + e.getMessage());
		} catch (Exception e) {		
			this.error("Error while executing configuration script " + configScriptLocation + ": " + e.getMessage());
		}
	}
	
	
	protected void loadGlobalRc(Engine engine, FileObject userDir) {
		//look in working folder
		String initFile = ".vfsserverrc";
		File file = new File(initFile);
		
		if (file.exists()) {
			
			CommandProvider oldRegister = engine.getCommandRegistry().getCommand("register");
			CommandProvider oldUnregister = engine.getCommandRegistry().getCommand("unregister");
			
			//temporarily add register
			CommandProvider tmpRegister = new Register();
			tmpRegister.register(engine.getCommandRegistry());
			CommandProvider tmpUnregister = new Unregister();
			tmpUnregister.register(engine.getCommandRegistry());
						
			try {
				this.println("Loading " + initFile);
				FileReader freader = new FileReader(file);
				engine.load(freader);
				this.println("Done loading");
			}
			catch(Exception e) {
				this.error("Error in script " + initFile + ": " + e.getMessage());
			}
			finally {
				if (oldRegister != null) {
					oldRegister.register(engine.getCommandRegistry());
				}
				else {
					tmpRegister.unregister(engine.getCommandRegistry());
				}
				if (oldUnregister != null) {
					oldUnregister.register(engine.getCommandRegistry());
				}
				else {
					tmpUnregister.unregister(engine.getCommandRegistry());
				}
			}
		}
	}
	
	protected void loadUserRc(Engine engine, FileObject root) throws FileSystemException {

		//get the name of the user init script
		String initFile = System.getProperty("org.dctmvfs.vfs.server.userscript", ".vfsshellrc");
		
		if (!"none".equals(initFile)) {

			//look in working folder
			FileObject file = root.resolveFile(initFile);
			
			if (file.exists()) {
				
				Reader reader = null;
				try {
					this.println("Loading " + initFile);
					reader = new InputStreamReader(file.getContent().getInputStream());
					engine.load(reader);
					this.println("Done loading");
				}
				catch(Exception e) {
					this.error("Error in script " + initFile + ": " + e.getMessage());
				}
				finally {
					if (reader!=null) {
						try {
							reader.close();
						}
						catch (IOException e) {
							//ignore
						}
					}
				}
			}
		}
	}

	
	

}
