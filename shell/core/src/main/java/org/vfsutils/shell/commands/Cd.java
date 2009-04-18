package org.vfsutils.shell.commands;

import java.util.Stack;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Cd extends AbstractDirManip implements CommandProvider {
	
	public Cd() {
		super("cd", new CommandInfo("Change the current working directory", "[(<path> | <~index>)]"));
	}		
		
	public void execute(Arguments args, Engine engine) throws CommandException, FileSystemException {
		
		if (args.size() >= 1) {
			String path = args.getArgument(0);
        	cd (path, engine);
        }
        else {
            cd (engine);
        }
        
	    printCwd(engine);
	}
	
	protected void cd(Engine engine) throws FileSystemException {
		String path = java.lang.System.getProperty("user.home");
		cd(path, engine);
	}
	
	protected void cd(String path, Engine engine) throws FileSystemException {
		if (path.startsWith("~") && path.length()>1){
			String subs = path.substring(1);
			//try to convert to int
			int i = Integer.parseInt(subs);
			cd(i, engine);
		}
		else {
			//Locate and validate the folder		
			FileObject tmp = engine.pathToExistingFile(path);    
			cd(tmp, engine);
		}
	}

	protected void cd(FileObject file, Engine engine) throws IllegalArgumentException, FileSystemException {		
		
		if (file.getType().equals(FileType.FILE)) {
            throw new IllegalArgumentException("Not a directory");
        }    
        
		engine.getContext().setCwd(file);
	}
	
	protected void cd(int i, Engine engine) throws IllegalArgumentException, FileSystemException {
		int absI = Math.abs(i);
		Stack stack = getStack(engine);
		
		if (absI > stack.size()) {
			throw new IllegalArgumentException("Invalid index given " + i);
		}		
		
		FileObject file;
		if (i==0) {
			file = engine.getCwd();
		}
		else if (i>0) {
			file = (FileObject) stack.get(stack.size()-i);
		}
		else if (absI == stack.size()){
			file = engine.getCwd();
		}
		else {
			file = (FileObject) stack.get(absI);
		}
		cd(file, engine);
	}
	
}
