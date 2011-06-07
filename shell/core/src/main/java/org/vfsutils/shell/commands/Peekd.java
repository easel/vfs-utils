package org.vfsutils.shell.commands;

import java.util.Stack;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Peekd extends AbstractDirManip {
	
	public Peekd() {
		super("peekd", new CommandInfo("Change the directory to the last from the stack", ""));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		peekd(engine);
		printCwd(engine);
	}
	
	protected void peekd(Engine engine) throws FileSystemException {
		Stack stack = getStack(engine);
		FileObject file = (FileObject) stack.peek();
		if (file!=null) {
			engine.getContext().setCwd(file);
		}
	}

}
