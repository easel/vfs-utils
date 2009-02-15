package org.vfsutils.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;

import org.apache.commons.vfs.FileSystemException;

import bsh.EvalError;
import bsh.Interpreter;

public class Shell implements bsh.ConsoleInterface {

	
	protected Engine engine;
	
	protected Reader reader;
	
	protected Shell() {
		//do nothing
	}
	
	public Shell(InputStream in) throws FileSystemException {
		
		this.engine = new Engine(this);
		this.reader = new InputStreamReader(in);
		customizeEngine(engine);
		loadRc();
	}
	
	protected void customizeEngine(Engine engine) {
				
		String configScriptLocation = System.getProperty("org.dctmvfs.vfs.shell.customscript", "vfs-shell-custom-init.bsh");

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
	
	
	protected void loadRc() {
		//look in working folder
		String initFile = ".vfsshellrc";
		File file = new File(initFile);
		if (!file.exists()) {
			initFile = System.getProperty("user.home") + File.separator + initFile;
			file = new File(initFile);
		}
		if (file.exists()) {
			try {
				this.println("Loading " + initFile);
				FileReader freader = new FileReader(file);
				this.engine.load(freader);
				this.println("Done loading");
			}
			catch(Exception e) {
				this.error("Error in script " + initFile + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public void go() throws Exception {
		
		try {
			this.engine.go();
		}
		finally {
			this.engine.close();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			if (args.length==0) {
				Shell shell = new Shell(System.in);
				shell.go();
			}
			else {
				String path = args[0];
				File file = new File(path);
				if (file.exists()) {
					InputStream in = null;
					try {
						in = new FileInputStream(file);
						Shell shell = new Shell(in);
						shell.go();
					}
					finally {
						try {
							if (in!=null) in.close();
						}
						catch (IOException e){}
					}
				}
			}
		}
		catch (Exception e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	    
		System.exit(0);
	}

	public void error(Object o) {
		System.err.println(o);		
	}

	public PrintStream getErr() {
		return System.err;
	}

	public Reader getIn() {
		return this.reader;
	}

	public PrintStream getOut() {
		return System.out;
	}

	public void print(Object o) {
		System.out.print(o);		
	}

	public void println(Object o) {
		System.out.println(o);		
	}

	
	
	
    
    
}
