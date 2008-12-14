package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Pwd extends AbstractDirManip {

	public Pwd() {
		super("pwd", new CommandInfo("Print current working directory", ""));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {

		pwd(engine);
	}
	
	protected void pwd(Engine engine) throws FileSystemException {
		engine.println(engine.toString(engine.getCwd()));
	}

}
