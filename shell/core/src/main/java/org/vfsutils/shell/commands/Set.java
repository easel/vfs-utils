package org.vfsutils.shell.commands;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Set extends AbstractCommand {

	public Set() {
		super("set", new CommandInfo("Set a variable", "[[-a]|<name>[=<value>]]"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		if (args.size()==0) {
			printAll(args.hasFlag("a"), engine);
		}
		else {
			String setting = args.getArgument(0);
			if (setting.indexOf('=')==-1) {
				String name = setting;
				validate(name);
				unset(name, engine);
			}
			else {
				String name = setting.substring(0, setting.indexOf('='));
				String value = setting.substring(setting.indexOf('=')+1);
				validate(name);
				set(name, value, engine);
			}
		}
	}

	
	protected void printAll(boolean all, Engine engine) {
		Map vars = engine.getContext().getAll();
		Iterator iterator = vars.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
			if (all || !key.toString().startsWith("vfs.")) {
				
				Object value = vars.get(key);
				if (value!=null) {
					engine.println(key.toString() + "=" + value.toString());
				}
			}
		}
	}
	
	protected void unset(String name, Engine engine) {
		
		Object oldValue = engine.getContext().get(name);
		engine.getContext().unset(name);
		
		engine.println("Unset " + name + " (was " + oldValue + ")");
	}
	
	protected void set(String name, String value, Engine engine) {
				
		Object oldValue = engine.getContext().get(name);
		engine.getContext().set(name, value);

		if (oldValue == null) {
			engine.println("Set " + name + " to " + value);
		}
		else {
			engine.println("Changed " + name + " from " + oldValue.toString() + " to " + value);
		}
	}
	
	protected void validate(String name) throws IllegalArgumentException{
		if (!name.matches("\\w+")) {
			throw new IllegalArgumentException(name + " is not accepted as a variable");
		}
	}

	
	

}
