package org.vfsutils.shell.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Load extends AbstractCommand implements CommandProvider {

	public Load() {
		super("load", new CommandInfo("Load a script", "[-c] <path>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
		
		args.assertSize(1);

		if (args.hasFlag("c")) {
			call(args, engine);
		}
		else {
			load(args, engine);
		} 
	}

	public void load(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {

		String path = (String) args.getArguments().get(0);
		
		final FileObject file = engine.pathToFile(path);
	
		if (!file.exists()) {
        	throw new IllegalArgumentException("File does not exist " + engine.toString(file));
        }
		
		Arguments largs = copyArgs(args);
		try {
			setArgs(engine, largs, true);
			load(file, engine, true);
		}
		finally {
			setArgs(engine, largs, false);
		}
	}

	public void load(final FileObject file, Engine engine, boolean haltOnError)
			throws FileSystemException {

		final InputStream in = file.getContent().getInputStream();

		boolean prevHaltOnError = engine.isHaltOnError();
		boolean prevEchoOn = engine.isEchoOn();
		try {
			engine.setHaltOnError(haltOnError);
			engine.setEchoOn(true);
			engine.load(new InputStreamReader(in));
		} catch (Exception e) {
			throw new FileSystemException(e);
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
			throws IllegalArgumentException, FileSystemException {
		
		String path = (String) args.getArguments().get(0);
		final FileObject file = engine.pathToFile(path);
		
		if (!file.exists()) {
        	throw new IllegalArgumentException("File does not exist " + engine.toString(file));
        }
		
		Arguments largs = copyArgs(args);
		
		Engine newEngine = new Engine(engine.getConsole(), engine
					.getCommandRegistry(), engine.getMgr());
			
		setArgs(newEngine, largs, true);
		call(file, newEngine);
		
		//no need to unset the arguments		
	}

	/**
	 * Executes the given script but does not change the current context
	 * 
	 * @param file
	 * @param engine
	 * @throws FileSystemException
	 */
	public void call(final FileObject file, Engine newEngine)
			throws FileSystemException {

		final InputStream in = file.getContent().getInputStream();

		try {			
			newEngine.load(new InputStreamReader(in));
		} catch (Exception e) {
			throw new FileSystemException(e);
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
			String arg = (String) argsIterator.next();
			if (index==0) {
				//the name of the script
				result.setCmd(args.getArgument(0));				
			}
			else {
				result.addArgument(arg);
			}
		}
		
		Iterator flagIterator = args.getFlags().iterator();
		while (flagIterator.hasNext()) {
			String flag = (String) flagIterator.next();
			if (flag.equals("c")) {
				//ignore it
			}
			else if (flag.length()==1){
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

	protected void setArgs(Engine engine, Arguments args, boolean set) {
		
		if (set) engine.getContext().set("cmd", args.getCmd());
		else engine.getContext().unset("cmd");
		
		if (set) engine.getContext().set("args", args.asString(1));
		else engine.getContext().unset("args");
		
		List a = args.getArguments();
		for (int i=0; i<a.size(); i++) {
			if (set) engine.getContext().set("arg" + i, a.get(i));
			else engine.getContext().unset("arg" + i);
		}
		
		if (set){ 
			Iterator fIterator = args.getFlags().iterator();
			StringBuffer buffer = new StringBuffer();
			while (fIterator.hasNext()) {
				String f = (String) fIterator.next();
				if (buffer.length()>0) buffer.append(" ");
				if (f.length()==1) {
					buffer.append("-").append(f);
				}
				else {
					buffer.append("--").append(f);
				}
			}
			
			engine.getContext().set("flags", buffer.toString());
		}
		else {
			engine.getContext().unset("flags");
		}
		
		if (set){
			Iterator oIterator = args.getOptions().keySet().iterator();
			StringBuffer buffer = new StringBuffer();
			while (oIterator.hasNext()) {
				String key = (String) oIterator.next();
				String value = (String) args.getOptions().get(key);
				if (buffer.length()>0) buffer.append(" ");
				buffer.append("--").append(key).append("=").append(value);
			}
			
			engine.getContext().set("options", buffer.toString());
		}
		else {
			engine.getContext().unset("options");
		}
	}
	
}
