package org.vfsutils.shell.commands;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileUtil;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Cat extends AbstractCommand implements CommandProvider {
	
	public Cat() {
		super("cat", new CommandInfo("Dump file content", "<path>"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(1);

		String path = args.getArgument(0);
		
        // Locate the file
        final FileObject file = engine.pathToExistingFile(path);
        
        cat(file, engine);
	}
	
	protected void cat(FileObject file, Engine engine) throws CommandException, FileSystemException {
		// Dump the contents to System.out
        try {
        	FileUtil.writeContent(file, engine.getConsole().getOut());
        }
        catch (IOException e) {
        	throw new CommandException(e);
        }
        engine.println("");
	}

}
