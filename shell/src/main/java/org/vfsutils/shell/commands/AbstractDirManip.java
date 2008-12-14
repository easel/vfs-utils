package org.vfsutils.shell.commands;

import java.util.Stack;

import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public abstract class AbstractDirManip extends AbstractCommand {

	protected int maxStackSize = 99;
	
	public AbstractDirManip(String cmd, CommandInfo info) {
		super(cmd, info);
	}

	protected void printCwd(Engine engine) {
		engine.println("Current folder is " + engine.toString(engine.getCwd()));
	}

	protected Stack getStack(Engine engine) {
		Object o = engine.getContext().get("vfs.dirstack");
		if (o==null){
			o = new Stack();
			engine.getContext().set("vfs.dirstack", o);
		}
		return (Stack) o;
	}

}