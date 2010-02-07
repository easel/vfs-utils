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
		protected boolean fail = true;
		protected int cntFiles = 0;
		protected int cntDirs = 0;
	}
	
	public Rm() {
		super("rm", new CommandInfo("Remove a file", "[-vF] <path> [--dry-run]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException,CommandException, FileSystemException {
	
		args.assertSize(1);
		
		String path = args.getArgument(0);
		
		RmOptions options = new RmOptions();

		options.verbose = args.hasFlag('v');
		options.dryRun = args.hasFlag("dry-run");
		options.fail = !args.hasFlag('F');
		
		if (path.startsWith("/*")) {
			throw new IllegalArgumentException("Removing all files from the root is disabled");
		}
		
		try {
			final FileObject[] files = engine.pathToFiles(path, true);
			rm(files, options, engine);
		}
		catch (IllegalArgumentException e) {
			handleException(e, options, engine);
		}
		
		engine.println((options.dryRun?"[Dry run] ":"") + "Removed " + options.cntDirs + " Folder(s), " + options.cntFiles + " File(s)");

	}
	
	public void rm(FileObject[] files, RmOptions options, Engine engine) throws FileSystemException, CommandException {

		for (int i=0; i< files.length; i++) {
			FileObject file = files[i];
			try {
				rm(file, options, engine);
			}
			catch (FileSystemException e) {
				handleException(e, options, engine);
			}
			catch (CommandException e) {
				handleException(e, options, engine);
			}
		}
	}
	
	
	public void rm(FileObject file, RmOptions options, Engine engine) throws FileSystemException, CommandException {
		
		if (!file.exists()) {
			fail("Could not delete " + engine.toString(file) + " because it does not exist", options, engine);
			return;
		}
		
		boolean isFile = file.getType().equals(FileType.FILE);
		boolean isDeleted = true;
		
		if (!options.dryRun) {			
			isDeleted = file.delete(Selectors.SELECT_SELF)>0;
			if (!isDeleted) {
				fail("Could not delete " + engine.toString(file), options, engine);
			}
		}
		
		if (isDeleted) {
			if (isFile) {
				options.cntFiles++;
			}
			else {
				options.cntDirs++;
			}
        
			verbose("Removed " + engine.toString(file), options, engine);
		}
        
	}
	
	protected void handleException(Exception e, RmOptions options, Engine engine) throws IllegalArgumentException, CommandException {
		if (options.fail) {
			if (e instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e;
			}
			else if (e instanceof CommandException) {
				throw (CommandException) e;
			}
			else {
				throw new CommandException(e);
			}
		}
		else {
			verbose(e.getMessage(), options, engine);
		}
	}
	
	protected void fail(String message, RmOptions options, Engine engine) throws CommandException {
		if (options.fail) {
			throw new CommandException(message);
		}
		else  {
			verbose(message, options, engine);
			return;
		}
	}
	
	protected void verbose(String message, RmOptions options, Engine engine) {
		if (options.verbose) {
        	engine.println(message);
        }
	}
	

}
