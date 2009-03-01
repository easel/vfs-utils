package org.vfsutils.shell.commands;

import java.util.Stack;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Popd extends AbstractDirManip {

	public Popd() {
		super("popd", new CommandInfo("Pop last directory from stack", "[-n]"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		boolean noCd = args.hasFlag("n");
		
		popd(noCd, engine);
		
		if (!noCd) {
			printCwd(engine);
		}
	}
	
	protected void popd(boolean noCd, Engine engine) throws FileSystemException {
		Stack stack = getStack(engine);
		if (stack.size()>0) {
			FileObject file = (FileObject) stack.pop();
			if (!noCd && file!=null) {
				engine.getContext().setCwd(file);
			}
		}
	}

}
