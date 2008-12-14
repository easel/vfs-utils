package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Selectors;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Rm extends AbstractCommand implements CommandProvider {
	
	public Rm() {
		super("rm", new CommandInfo("Remove a file", "<path>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
	
		args.assertSize(1);
		
		String path = args.getArgument(0);

        final FileObject file = engine.pathToFile(path);
        file.delete(Selectors.SELECT_SELF);

	}
	
	

}
