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

public class Rm extends AbstractCommand implements CommandProvider {
	
	public class RmOptions {
		protected boolean verbose = false;
		protected boolean dryRun = false;
		protected int cntFiles = 0;
		protected int cntDirs = 0;
	}
	
	public Rm() {
		super("rm", new CommandInfo("Remove a file", "[-v] <path> [--dry-run]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException,CommandException, FileSystemException {
	
		args.assertSize(1);
		
		String path = args.getArgument(0);
		
		RmOptions options = new RmOptions();

		options.verbose = args.hasFlag('v');
		options.dryRun = args.hasFlag("dry-run");
		
		if (path.equals("/**/*.*")) {
			throw new IllegalArgumentException("Removing all files is disabled");
		}
		
		final FileObject[] files = engine.pathToFiles(path);
		
		rm(files, options, engine);
		
		engine.println((options.dryRun?"[Dry run] ":"") + "Removed " + options.cntDirs + " Folder(s), " + options.cntFiles + " File(s)");

	}
	
	public void rm(FileObject[] files, RmOptions options, Engine engine) throws FileSystemException, CommandException {
		//iterate from the back to be able to remove folders
		for (int i = files.length-1; i >=0; i--) {
			FileObject file = files[i];
			rm(file, options, engine);
		}
	}
	
	public void rm(FileObject file, RmOptions options, Engine engine) throws FileSystemException, CommandException {
		
		boolean isFile = file.getType().equals(FileType.FILE);
		
		if (!options.dryRun) {
			if (file.delete(Selectors.SELECT_SELF)>0) {
				if (isFile) {
					options.cntFiles++;
				}
				else {
					options.cntDirs++;
				}
			}
			else {
				throw new CommandException("Could not delete " + engine.toString(file));
			}
		}
        
        if (options.verbose) {
        	engine.println("Removed " + engine.toString(file));
        }
	}
	

}
