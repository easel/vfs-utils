package org.vfsutils.shell.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.Arguments.Argument;
import org.vfsutils.shell.Arguments.Flag;
import org.vfsutils.shell.Arguments.Option;

public class Load extends AbstractCommand implements CommandProvider {

	public Load() {
		super("load", new CommandInfo("Load a script", "[-ce] [--encoding=<enc>] <path>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(1);
		
		if (args.hasFlag("c")) {
			call(args, engine);
		}
		else {
			load(args, engine);
		} 
	}

	public void load(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {

		String path = args.getArgument(0);
		
		final FileObject file = engine.pathToExistingFile(path);
		
		boolean echo = args.hasFlag('e');
		
		String encoding = args.getOption("encoding");
		
		Arguments largs = copyArgs(args);
		load(file, encoding, largs, engine, true, echo);
	}

	public void load(final FileObject file, String encoding, Arguments args, Engine engine, boolean haltOnError, boolean echo)
			throws CommandException, FileSystemException {

		final InputStream in = file.getContent().getInputStream();

		load(in, encoding, args, engine, haltOnError, echo);
	}
	
	public void load(final InputStream in, String encoding, Arguments args, Engine engine, boolean haltOnError, boolean echo)
		throws CommandException, FileSystemException {

		boolean prevHaltOnError = engine.isHaltOnError();
		boolean prevEchoOn = engine.isEchoOn();
		try {
			engine.setHaltOnError(haltOnError);
			engine.setEchoOn(echo);
			
			try {
				//make the arguments in the context
				setArgs(engine, args, true);
				//load
				if (encoding == null) {
					engine.load(new InputStreamReader(in));
				}
				else {
					engine.load(new InputStreamReader(in, encoding));
				}
			}
			finally {
				//remove the arguments
				setArgs(engine, args, false);
			}
		} catch (Exception e) {
			throw new CommandException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
				;
			}
			engine.setHaltOnError(prevHaltOnError);
			engine.setEchoOn(prevEchoOn);
		}

	}


	
	public void call(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		String path = args.getArgument(0);
		final FileObject file = engine.pathToExistingFile(path);
		
		boolean echo = args.hasFlag('e');
		
		String encoding = args.getOption("encoding");
		
		Arguments largs = copyArgs(args);
		
		Engine newEngine = new Engine(engine.getConsole(), engine
					.getCommandRegistry(), engine.getMgr());
		
		newEngine.setEchoOn(echo);
			
		setArgs(newEngine, largs, true);
		call(file, encoding, newEngine);
		
		//no need to unset the arguments		
	}

	/**
	 * Executes the given script but does not change the current context
	 * 
	 * @param file
	 * @param encoding
	 * @param newEngine
	 * @throws FileSystemException
	 */
	public void call(final FileObject file, String encoding, Engine newEngine)
			throws CommandException, FileSystemException {

		final InputStream in = file.getContent().getInputStream();

		try {			
			if (encoding==null) {
				newEngine.load(new InputStreamReader(in));
			}
			else {
				newEngine.load(new InputStreamReader(in, encoding));
			}
			
		} catch (Exception e) {
			throw new CommandException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
				;
			}
		}
	}
	
	protected Arguments copyArgs(Arguments args) {
		Arguments result = new Arguments();
		
		ListIterator argsIterator = args.getArguments().listIterator();
		while (argsIterator.hasNext()) {
			int index = argsIterator.nextIndex();
			Argument arg = (Argument) argsIterator.next();
			if (index==0) {
				//the name of the script
				result.setCmd(arg.getValue());				
			}
			else {
				result.addArgument(arg);
			}
		}
		
		Iterator flagIterator = args.getFlags().iterator();
		while (flagIterator.hasNext()) {
			Flag flag = (Flag) flagIterator.next();
			if (flag.getValue().equals("c") || flag.getValue().equals("e")) {
				//ignore it
			}
			else {
				result.addFlag(flag);
			}
		}
		
		Iterator optionIterator = args.getOptions().keySet().iterator();
		while (optionIterator.hasNext()) {
			String key = (String) optionIterator.next();
			if (key.equals("flags")) {
				Option option = (Option) args.getOptions().get(key);
				char[] flags = option.getValue().toCharArray();
				//each flag should be added individually
				for (int i=0; i<flags.length; i++) {
					result.addFlag(String.valueOf(flags[i]));
				}
			}
			else if (key.equals("encoding")) {
				//ignore
			}
			else {
				Option option = (Option) args.getOptions().get(key);
				result.addOption(option);
			}
		}			
		
		return result;
	}

	protected void setArgs(Engine engine, Arguments args, boolean set) {
		
		if (set) engine.getContext().set("cmd", args.getCmd());
		else engine.getContext().unset("cmd");
		
		if (set) engine.getContext().set("args", engine.getCommandParser().toString(args, 1));
		else engine.getContext().unset("args");
		
		List a = args.getArguments();
		for (int i=0; i<a.size(); i++) {
			if (set) engine.getContext().set("arg" + (i+1), a.get(i));
			else engine.getContext().unset("arg" + (i+1));
		}
		
		if (set){ 
			String flags = engine.getCommandParser().toString(args.getFlags());
			engine.getContext().set("flags", flags);
		}
		else {
			engine.getContext().unset("flags");
		}
		
		if (set){
			String options = engine.getCommandParser().toString(args.getOptions());
			engine.getContext().set("options", options);
		}
		else {
			engine.getContext().unset("options");
		}
	}
	
}
