package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
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
	 * Allow registering VFS scripts. If the script 
	 */
	protected void registerScript(FileObject file, String name,
			String description, String usage, String type, Engine engine) {

		FileName fileName = file.getName();

		String extension = fileName.getExtension();

		if (name == null) {
			String baseName = fileName.getBaseName();
			name = (extension.length() == 0 ? baseName : baseName.substring(0,
					baseName.length() - extension.length() - 1));
		}

		if (description == null) {
			description = "script " + fileName.toString();
		}
		if (usage == null) {
			usage = "";
		}

		Script script = new Script(name, new CommandInfo(description, usage));
		script.path = fileName.toString();
		
		if (type != null && type.equals("bsh")) {
			throw new IllegalArgumentException("Beanshell scripts are not supported in boxed mode");
		}
		else if (type != null && type.equals("vfs")) {
			script.type = type;
		} else if (extension.equals("bsh")) {
			throw new IllegalArgumentException("Beanshell scripts are not supported in boxed mode");
		} else {
			// the default
			script.type = "vfs";
		}

		CommandProvider command = engine.getCommandRegistry().getCommand(script.cmd);
		boolean allow = false;
		if (command==null) {
			allow = true;
		} else {
			//only vfs scripts can be overwritten
			if (command instanceof Register.Script) {
				if ("vfs".equals(((Register.Script)command).type)) {
					allow = true;
				}
			}
		}

		if (allow) {
			script.register(engine.getCommandRegistry());
			engine.println("Registered " + script.type + " script "
				+ fileName.toString() + " as " + name);
		}
		else {
			engine.error("Registering script " + fileName.toString() + " as " + name + " is not allowed");
		}
	}

	
	public void registerClass(String className, String name,
			String description, String usage, Engine engine)
			throws CommandException {
	
		throw new IllegalArgumentException("Registering classes is not supported in boxed mode");
	}


}
