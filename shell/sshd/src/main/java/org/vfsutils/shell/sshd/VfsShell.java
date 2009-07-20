package org.vfsutils.shell.sshd;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.sshd.server.ShellFactory.Environment;
import org.apache.sshd.server.ShellFactory.ExitCallback;
import org.apache.sshd.server.ShellFactory.Shell;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.shell.boxed.BoxedCommandRegistry;
import org.vfsutils.shell.boxed.BoxedEngine;

import bsh.ConsoleInterface;

public class VfsShell implements Shell, ConsoleInterface, Runnable {
	
	protected static final Logger log = LoggerFactory.getLogger(VfsShell.class);

	private PrintStream out = null;
	private PrintStream err = null;
	private Reader in = null;
	
	private ExitCallback callback;
	private Thread thread;
	
	private FileSystemManager fsManager;
	private BoxedEngine engine;
	private FileObject root;
	private String path;
		
	/**
	 * Initialize with an optional path. This path can be overridden by the session if it contains
	 * a value for VfsShellFactory.VFS_PATH.
	 * If there is no root set in the session with VfsShellFactory.VFS_ROOT then the path will be 
	 * resolved as an absolute path; if the root has been set then the path will be resolved 
	 * within that root.
	 * The path can contain the alias $USER which will be replaced by the user name. 
	 * @param fsManager
	 * @param rootPath the path to resolve, it can contain the $USER token and can be null
	 */
	public VfsShell(FileSystemManager fsManager, String path) {
		this.fsManager = fsManager;
		this.path = path;
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
		
		//TEMP: intialize session values
		FileObject storedRoot = VfsShellFactory.vfsRoot.get();
		//rest
		VfsShellFactory.vfsRoot.set(null);
		if (storedRoot != null) {
			this.root = storedRoot;
		}
		
		String storedPath = VfsShellFactory.vfsPath.get();
		//reset
		VfsShellFactory.vfsPath.set(null);
		if (storedPath != null) {
			this.path = storedPath;
		}
		
		String term = env.getEnv().get("TERM");
		log.debug("Client terminal: " + term);
		
		if (term != null && term.toLowerCase().startsWith("xterm")) {
			this.in = new EchoReader(this.in, this);
			log.debug("Activated echo");
		}
		
		String name;
		if (this.path!=null && this.path.contains("$USER")) {
			name = this.path.replaceAll("\\$USER", env.getEnv().get("USER"));
		}
		else {
			name = this.path;
		}
		
		//update the root
		if (this.root == null && name!=null) {
			this.root = fsManager.resolveFile(name);
		}
		else if (this.root!=null && name!=null) {
			FileObject startDir = this.root.resolveFile(name);
			if (startDir.exists()) {
				this.root = startDir;
			}
			else {
				log.warn("Invalid path, directory does not exist:" + startDir);
			}
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
			engine = new BoxedEngine(this, new BoxedCommandRegistry(), this.fsManager);	
			
			engine.setStartDir(this.root);
			//engine.setEchoOn(true);
			engine.go();
		} catch (Exception e) {
			//TODO: handle errors
			e.printStackTrace();
		}
		finally {
			callback.onExit(0);
		}
	}

	public void setSession(ServerSession session) {
		
		FileObject storedRoot = session.getAttribute(VfsShellFactory.VFS_ROOT);
		if (storedRoot != null) {
			this.root = storedRoot;
		}
		
		String storedPath = session.getAttribute(VfsShellFactory.VFS_PATH);
		if (storedPath != null) {
			this.path = storedPath;
		}
		
	}
	
	

}
