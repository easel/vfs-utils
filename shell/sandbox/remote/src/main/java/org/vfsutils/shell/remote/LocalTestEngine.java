package org.vfsutils.shell.remote;

import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;

public class LocalTestEngine extends Engine {

	private CommandRunner runner;
	
	public LocalTestEngine(ConsoleInterface console) throws FileSystemException {
		super(console);
		try {
			this.runner = new CommandRunner();
			Engine remoteEngine = new Engine(runner);
			this.runner.startEngine(remoteEngine);
		}
		catch (IOException e) {
			throw new FileSystemException(e);
		}
	}

	
	
	public boolean handleCommand(Arguments args) {
		
		try {
			ShellRequest request = new ShellRequest(args.toString());
			ShellResponse response = runner.handleInput(request);
			
			String out = response.getOut();
			//make sure to use print and not println!
			if (out!=null) {
				this.print(out);
			}
			
			String err = response.getErr();
			if (err!=null && err.length()>0) {
				this.error(err);
			}			
		}
		catch (IOException e) {
			error(e.getMessage());
			if (haltOnError) {
				return false;
			}
		}
		return true;
	}
	
	
	
	

}
