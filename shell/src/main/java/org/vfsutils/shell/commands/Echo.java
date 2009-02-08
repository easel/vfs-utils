package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Echo extends AbstractCommand {

	public Echo() {
		super("echo", new CommandInfo("Writes all arguments to output", "<expression>"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		echo(args, engine);
	}
	
	public void echo(Arguments args, Engine engine) {
		engine.println(args.asString(1));
	}

}
