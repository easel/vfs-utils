package org.vfsutils.shell.mina1;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;

public class LocalEngine extends Engine {

		
	ShellClientHandler handler;
	
	public LocalEngine(ConsoleInterface console) throws FileSystemException {
		super(console);
		try {
			String host = "localhost";
			int port = 9123;
			
			
	        handler = new ShellClientHandler(host, port, this);
	        handler.connect();        

		}
		finally{}
		
	}

	
	
	public boolean handleCommand(Arguments args) {
		
		handler.sendRequest(args.toString());
				
		return true;
	}
	
	
	
	

}
