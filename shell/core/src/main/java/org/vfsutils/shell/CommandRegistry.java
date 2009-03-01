package org.vfsutils.shell;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CommandRegistry {
	
	protected Map reg = new HashMap();
	
	/**
	 * Registers the CommandProvider with the command. If the command has
	 * been registered before the previous entry will be overwritten.
	 * @param cmd
	 * @param command
	 */
	public void registerCommand(String cmd, CommandProvider command) {
		this.reg.put(cmd, command);
	}
	
	/**
	 * Unregisters the CommandProvider for the command, but only if the
	 * CommandProvider matches the one registered.
	 * @param cmd
	 * @param command
	 */
	public void unregisterCommand(String cmd, CommandProvider command) {
		CommandProvider tmp = getCommand(cmd);
		if (tmp!=null && tmp.equals(command)) {
			this.reg.remove(cmd);
		}
	}
	
	/**
	 * Retrieve the command from the registry
	 * @param cmd
	 * @return
	 */
	public CommandProvider getCommand(String cmd) {
		return (CommandProvider) reg.get(cmd);
	}
	
	/**
	 * Returns all the registered commands
	 * @return
	 */
	public Set getAllCommands() {
		return new TreeSet(this.reg.keySet());
	}
	
}
