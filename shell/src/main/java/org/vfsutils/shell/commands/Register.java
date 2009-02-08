package org.vfsutils.shell.commands;

import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Register extends AbstractCommand {

	protected class Script extends AbstractCommand {
		
		String path = null;
		String type = "vfs";
		
		public Script(String cmd, CommandInfo info) {
			super(cmd, info);
		}

		public void execute(Arguments args, Engine engine)
				throws IllegalArgumentException, CommandException,
				FileSystemException {
			
			Arguments largs = copyArgs(this, args);
			engine.handleCommand(largs);
			
		}
		
	}
	
	public Register() {
		super("register", new CommandInfo("Registers a script", "<path>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(1);
		
		FileObject[] files = engine.pathToFiles(args.getArgument(0));
		
		if (files.length==0) {
			engine.println("No files selected");
		}
		else if (files.length==1) {
			register(files[0], args.getOption("name"), args.getOption("description"), args.getOption("usage"), args.getOption("type"), engine);
		}
		else {
			register(files, engine);
		}
	}
	
	protected void register(FileObject[] files, Engine engine) {
		for (int i=0; i<files.length; i++) {
			register(files[i], engine);
		}
	}
	
	protected void register(FileObject file, Engine engine) {
		register(file, null, null, null, null, engine);
	}
	
	protected void register(FileObject file, String name, String description, String usage, String type, Engine engine) {
		
		FileName fileName = file.getName();
		
		String extension = fileName.getExtension();
		
		if (name==null) {
			String baseName = fileName.getBaseName();
			name = (extension.length()==0?baseName:baseName.substring(0, baseName.length() - extension.length() -1));
		}
		
		if (description==null) {
			description = "script " + fileName.toString();
		}
		if (usage==null) {
			usage = "";
		}
		
		Script script = new Script(name, new CommandInfo(description, usage));
		script.path = fileName.toString();
		
		if (type!=null && (type.equals("vfs") || type.equals("bsh"))) {
			script.type = type;
		}
		else if (extension.equals("bsh")) {
			script.type = "bsh";
		}
		else {
			//the default
			script.type = "vfs";
		}
		
		script.register(engine.getCommandRegistry());
		engine.println("Registered script " + fileName.toString() + " as " + name);
	}

	protected Arguments copyArgs(Script script, Arguments args) {
		Arguments result = new Arguments();
		
		if (script.type.equals("bsh")) {
			result.setCmd("bsh");
		}
		else {
			result.setCmd("load");
		}
		
		result.addArgument(script.path);
		
		ListIterator argsIterator = args.getArguments().listIterator();
		while (argsIterator.hasNext()) {
			String arg = (String) argsIterator.next();
			result.addArgument(arg);
		}
		
		Iterator flagIterator = args.getFlags().iterator();
		while (flagIterator.hasNext()) {
			String flag = (String) flagIterator.next();
			
			if (flag.length()==1){
				result.addFlags("-" + flag);
			}
			else {
				result.addLongFlag("--" + flag);
			}
		}
		
		Iterator optionIterator = args.getOptions().keySet().iterator();
		while (optionIterator.hasNext()) {
			String key = (String) optionIterator.next();
			if (key.equals("flags")) {
				String value = (String) args.getOptions().get(key);
				result.addFlags("-" + value);
			}
			else {
				String value = (String) args.getOptions().get(key);
				result.addOption("--" + key + "=" + value);
			}
		}			
		
		return result;
	}
}
