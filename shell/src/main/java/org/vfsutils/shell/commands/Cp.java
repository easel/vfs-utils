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

		String srcPath = args.getArgument(0);
		String destPath = args.getArgument(1);
		
        final FileObject src = engine.pathToFile(srcPath);
        FileObject dest = engine.pathToFile(destPath);
        
        if (dest.exists() && dest.getType() == FileType.FOLDER) {
            dest = dest.resolveFile(src.getName().getBaseName());
        }

        dest.copyFrom(src, Selectors.SELECT_ALL);

	}

}
