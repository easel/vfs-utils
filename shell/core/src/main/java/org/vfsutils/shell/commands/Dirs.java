package org.vfsutils.shell.commands;

import java.util.Stack;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Dirs extends AbstractDirManip {

	public Dirs() {
		super("dirs", new CommandInfo("Print the directory stack", ""));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		dirs(engine);
	}
	
	/**
	 * Print the stack starting with 1 for oldest item
	 * @param engine
	 * @throws FileSystemException
	 */
	protected void dirs(Engine engine) throws FileSystemException {
		Stack stack = getStack(engine);
		engine.println("Directory stack (newest first)");
		engine.println("  [0] " + engine.toString(engine.getCwd()));
		for (int i = 1; i<=stack.size(); i++) {
			FileObject file = (FileObject) stack.get(stack.size()-i);
			engine.println(" "+ (i>9?"":" ") + "[" + i + "] " + engine.toString(file));
		}
	}

}
