package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Echo extends AbstractCommand {

	public Echo() {
		super("echo", new CommandInfo("Writes all arguments to output", "<expression> | --on | --off | --status"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
	
		
		if (args.hasFlag("status")) {
			showEchoStatus(engine);
		}
		else if (args.hasFlag("off")) {
			setEchoOn(false, engine);
		}
		else if (args.hasFlag("on")) {
			setEchoOn(true, engine);
		}
		else {
			echo(args, engine);
		}
	}
	
	public void echo(Arguments args, Engine engine) {
		String echo = engine.getCommandParser().toString(args, 1, true); 
		engine.println(echo);
	}
	

	public void setEchoOn(boolean active, Engine engine) {
		engine.setEchoOn(active);
	}
	
	public void showEchoStatus(Engine engine) {
		engine.println("echo is " +  (engine.isEchoOn()?"on":"off"));
	}
}
