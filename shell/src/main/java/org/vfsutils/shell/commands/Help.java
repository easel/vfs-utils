package org.vfsutils.shell.commands;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.CommandRegistry;
import org.vfsutils.shell.Engine;

public class Help extends AbstractCommand implements CommandProvider {

	public Help() {
		super("help", new CommandInfo("Show help", ""));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
		
		
		if (args.getArguments().size()==0) {
			help(engine);
		}
		else {
			help(args.getArgument(0), engine);
		}

	}
	
	protected void help(Engine engine) throws FileSystemException {
		CommandRegistry reg = engine.getCommandRegistry();
		Set commands = reg.getAllCommands();
		
		Iterator iterator = commands.iterator();
		engine.println("Available commands: ");
		while (iterator.hasNext()) {
			String command = (String) iterator.next();
			if (command != null) {
				engine.println(command);
			}
		}
	}
	
	protected void help(String cmd, Engine engine) throws FileSystemException {
		CommandRegistry reg = engine.getCommandRegistry();
		CommandProvider command = reg.getCommand(cmd);
		if (command!=null) {
			engine.println(command.getCommand() + ": " + command.getDescription());
			engine.println("usage: " + command.getUsage());
		}
		else {
			engine.error("Unknow command: " + cmd);
		}
	}

}
