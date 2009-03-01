package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Cp extends AbstractCommand implements CommandProvider {

	
	public Cp() {
		super("cp", new CommandInfo("Copies an item", "<src> <dest>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(2);

		String srcPattern = args.getArgument(0);
		String destPath = args.getArgument(1);
		
        FileObject dest = engine.pathToFile(destPath);
        
        final FileObject[] files = engine.pathToFiles(srcPattern);

		if (files.length == 0) {
			throw new IllegalArgumentException("File does not exist: " + srcPattern);
		}
		else if (files.length == 1) {
			FileObject src = files[0];
		
			//if the target is a folder, the new file will be put within the folder
			FileObject newFile;
			if (dest.exists() && dest.getType() == FileType.FOLDER) {
	            newFile = dest.resolveFile(src.getName().getBaseName());
	        }
			else {
				newFile = dest;
			}
        	newFile.copyFrom(src, Selectors.SELECT_ALL);
		}
		else if (dest.exists() && dest.getType() == FileType.FOLDER) {
			//there are multiple src files, this can only be handled when the
			//target is an existing folder
			for (int i = 0; i < files.length; i++) {
				FileObject src = files[i];
				//we do not create relative paths within the target folder
				FileObject newFile = dest.resolveFile(src.getName().getBaseName());
				
				newFile.copyFrom(src, Selectors.SELECT_ALL);
			}
        }        
        else {
        	throw new IllegalArgumentException("Invalid arguments");
        }

	}

}
