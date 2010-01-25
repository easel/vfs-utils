package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Assert extends AbstractCommand {

	public Assert() {
		super("assert", new CommandInfo("Assert existence", "(-nfd)<path> [--cwd=<path>]"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(1);

		String path = args.getArgument(0);
				
        // Locate the file
        final FileObject file = engine.pathToFile(path);

        boolean notExists = args.hasFlag("n");
        boolean assertFile = args.hasFlag("f");
        boolean assertDir = args.hasFlag("d");
        
        String cwd = args.getOption("cwd");
        
        assertExists(file, notExists, engine);
        
        if (assertFile || assertDir) {
	        assertType(file, assertFile, engine);
        }
        
        if (cwd!=null && cwd.length()>0) {
        	assertSame(engine.pathToExistingFile(cwd), engine.getCwd(), engine);
        }
        
	}

	protected void assertExists(FileObject file, boolean negate, Engine engine) throws CommandException, FileSystemException {
		if (negate && file.exists()) {
       		throw new CommandException("File exists " + engine.toString(file));
        }
        else if (!file.exists()) {
        	throw new CommandException("File does not exist " + engine.toString(file));
        }
	}
	
	protected void assertType(FileObject file, boolean isFile, Engine engine) throws CommandException, FileSystemException {
		FileType type = file.getType(); 
        if (isFile) {
        	if (!(type.equals(FileType.FILE) || type.equals(FileType.FILE_OR_FOLDER))){
        		throw new CommandException("Not a file");
        	}
        }
        else {
        	if (!(type.equals(FileType.FOLDER) || type.equals(FileType.FILE_OR_FOLDER))){
        		throw new CommandException("Not a directory");
        	}
        }
	}

	protected void assertSame(FileObject expected, FileObject actual, Engine engine) throws CommandException {
		if (!expected.equals(actual)) {
			throw new CommandException("Expected " + engine.toString(expected) + " but received " + engine.toString(actual));
		}
	}
}
