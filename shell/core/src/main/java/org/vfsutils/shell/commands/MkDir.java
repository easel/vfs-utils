package org.vfsutils.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class MkDir extends AbstractCommand implements CommandProvider {

	public MkDir() {
		super ("mkdir", new CommandInfo("Create a directory", "<path>"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(1);

		String path = args.getArgument(0);
		
		final FileObject file = engine.pathToFile(path);
		if (file.exists()) {
			
			if (file.getType().equals(FileType.FILE)) {
				throw new CommandException("File already exists " + path);
			}
			else {
				engine.println("Folder already exists " + path);
			}
		}
		else {
			file.createFolder();
			engine.println("Folder " + path + " created");
		}
		
	}

}
