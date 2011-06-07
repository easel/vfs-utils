package org.vfsutils.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

/**
 * Provides a register command with limited functionality, it only allows vfs
 * shells to be registered
 * 
 * @author kleij - at - users.sourceforge.net
 * 
 */
public class BoxedRegister extends Register {

	/**
	 * Allow registering VFS scripts.  
	 * @throws CommandException 
	 */
	protected void doRegisterScript(FileObject file, String name,
			String description, String usage, String type, boolean cache, Engine engine) throws CommandException {

		if ("bsh".equals(type)) {
			throw new IllegalArgumentException("Beanshell scripts are not supported in boxed mode");
		} 
		else {
			//only vfs scripts can be overwritten
			CommandProvider command = engine.getCommandRegistry().getCommand(name);
			boolean allow = false;
			if (command==null) {
				allow = true;
			} else if (command instanceof Register.Script) {
				if ("vfs".equals(((Register.Script)command).getType())) {
					allow = true;
				}
			}

			if (allow) {
				super.doRegisterScript(file, name, description, usage, type, cache, engine);
			}
			else {
				throw new CommandException("Command " + name + " can not be redefined"); 
			}
		}

	}

	
	public void registerClass(String className, String name,
			String description, String usage, Engine engine)
			throws CommandException {
	
		throw new IllegalArgumentException("Registering classes is not supported in boxed mode");
	}


}
