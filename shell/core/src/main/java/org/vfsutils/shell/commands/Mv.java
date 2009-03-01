package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Mv extends AbstractCommand implements CommandProvider {

	public Mv() {
		super("mv", new CommandInfo("Move a file", "<src> <dest>"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {

		args.assertSize(2);

		String srcPath = args.getArgument(0);
		String destPath = args.getArgument(1);
		
        move (srcPath, destPath, engine);
	}
	
	protected void move(String srcPath, String destPath, Engine engine) throws FileSystemException {
		final FileObject src = engine.pathToFile(srcPath);
        FileObject dest = engine.pathToFile(destPath);
        
        move(src, dest, engine);
	}
	
	protected void move(FileObject src, FileObject dest, Engine engine) throws FileSystemException {
		if (dest.exists() && dest.getType() == FileType.FOLDER) {
            dest = dest.resolveFile(src.getName().getBaseName());
        }

        dest.moveTo(src);

	}

}
