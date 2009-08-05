package org.vfsutils.shell.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

/**
 * Allows unregistering commands 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class Unregister extends AbstractCommand {

	public Unregister() {
		super("unregister", "Unregisters commands", "(cmd*) [--keep]|--all");
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		boolean all = args.hasFlag("all");		
		boolean keep = args.hasFlag("keep");
		
		List cmds = argsToList(args);
		
		if (all) {
			unregisterAll(null, engine);
		}
		else if (keep) {
			unregisterAll(cmds, engine);
		}
		else {
			unregister(cmds, engine);
		}
	}
	
	/**
	 * Unregisters all commands, except register, unregister and help
	 * commands and the commands in the keep list
	 * @param engine
	 */
	public void unregisterAll(List keep, Engine engine) {
		Iterator iterator = engine.getCommandRegistry().getAllCommands().iterator();
		while (iterator.hasNext()) {
			String cmd = (String) iterator.next();
			if (keep==null || !keep.contains(cmd)) {
				unregister(cmd, true, engine);
			}
		}
	}
	
	/**
	 * Unregisters the list of commands
	 * @param cmds
	 * @param engine
	 */
	public void unregister(List cmds, Engine engine) {
		Iterator iterator = cmds.iterator();
		while (iterator.hasNext()) {
			String cmd = (String) iterator.next();
			unregister(cmd, false, engine);
		}
	}
	
	/**
	 * Unregisters the command, but not when it is a Register, Unregister or Help command
	 * @param cmd
	 * @param engine
	 */
	public void unregister(String cmd, boolean safe, Engine engine) {
		CommandProvider command = engine.getCommandRegistry().getCommand(cmd);
		if (command==null) {
			// do nothing
		} 
		else if (!safe) {
			unregister(cmd, command, engine);
		} 
		else if (!(command instanceof Register) 
				&& !(command instanceof Unregister)
				&& !(command instanceof Help)) {
			unregister(cmd, command, engine);
		}
	}
	
	/**
	 * Utility method, override this method when you want to change the
	 * behavior for both a normal and a safe unregister call 
	 * @param cmd
	 * @param command
	 * @param engine
	 */
	protected void unregister(String cmd, CommandProvider command, Engine engine) {
		if (command!=null) {
			engine.getCommandRegistry().unregisterCommand(cmd, command);
			engine.println("Unregistered " + cmd);
		}
		else {
			engine.println(cmd + " was not registered");
		}
	}
	
	private List argsToList(Arguments args) {
		List result = new ArrayList();
		for (int i=0; i<args.size(); i++) {
			result.add(args.getArgument(i));
		}
		return result;
	}

}
