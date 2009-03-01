package org.vfsutils.shell;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.vfsutils.shell.commands.Open;

public class BoxedShell extends Shell {

	public BoxedShell(InputStream in, String path, boolean askUsername, boolean askPassword, boolean askDomain, boolean virtual) throws FileSystemException {		
		this.engine = new BoxedEngine(this, new BoxedCommandRegistry(), VFS.getManager());
		this.reader = new InputStreamReader(in);
		customizeEngine(engine);
		loadRc();
		
		try {
			Open openCmd = new Open();
			openCmd.open(path, askUsername, askPassword, askDomain, virtual, engine);
		}
		catch (CommandException e) {
			throw new FileSystemException(e);
		}
	}
	
	public static void main(String[] args) {
		

		CommandParser parser = new CommandParser();
		Arguments arguments = parser.parse(args);
		
		try {
			BoxedShell shell = new BoxedShell(System.in, arguments.getCmd(), 
					arguments.hasFlag("u"),
					arguments.hasFlag("p"),
					arguments.hasFlag("d"),
					arguments.hasFlag("virtual"));
			
			shell.go();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		 
	}

	
}
