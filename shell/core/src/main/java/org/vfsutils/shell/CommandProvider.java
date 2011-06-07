package org.vfsutils.shell;

import org.apache.commons.vfs2.FileSystemException;

/**
 * A CommandProvider object can support several different commands. 
 * Typically closely related commands are provided by the same CommandProvider,
 * e.g. cd, pushd, popd. A CommandProvider can also provide aliases such as ls
 * and dir.
 * It is good practice to let the command names (e.g. ls or dir) be configurable
 * from outside the command.
 * @author kleij - at - users.sourceforge.net
 *
 */
public interface CommandProvider {
	
	
	/**
	 * Retrieves the command that this CommandProvider supports
	 * @return the command
	 */
	public abstract String getCommand();

	/**
	 * Executes the command specified with in the arguments. If the command is not supported
	 * the resulting behaviour can be arbitrary. 
	 * @param args
	 * @param engine
	 * @throws IllegalArgumentCommandException if one of arguments is incorrect
	 * @throws FileSystemException
	 */
	public abstract void execute(Arguments args, Engine engine) 
		throws IllegalArgumentException, CommandException, FileSystemException;
	
	
	/**
	 * Registers the command in the registry
	 * @param reg
	 */
	public abstract void register(CommandRegistry reg);
	
	/**
	 * Unregisters the command from the registry
	 * @param reg
	 */
	public abstract void unregister(CommandRegistry reg);
	
	
	/**
	 * Returns the usage details of the command
	 * @return
	 */
	public abstract String getUsage();

	/**
	 * Returns the description of the command
	 * @return
	 */
	public abstract String getDescription();
	
}
