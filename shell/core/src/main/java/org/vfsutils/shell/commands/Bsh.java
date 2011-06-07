package org.vfsutils.shell.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.Arguments.Argument;
import org.vfsutils.shell.Arguments.Flag;
import org.vfsutils.shell.Arguments.Option;

import bsh.EvalError;
import bsh.Interpreter;

public class Bsh extends AbstractCommand {

	public Bsh() {
		super("bsh", new CommandInfo("Execute a bsh file or expression", "<path>| -e <expression>"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(1);
		
		if (args.hasFlag("e")) {
			//concat all arguments, except command and first flags, unescape whitespace and quotes
			String script = engine.getCommandParser().toString(args, 2, true);	
			bsh(script, engine);			
		}
		else {
			FileObject file = engine.pathToExistingFile(args.getArgument(0));
			
			//copy the arguments
			Arguments largs = copyArgs(args);
			
			bsh(file, largs, engine);
		}			
	}
	
	protected void bsh(String expression, Engine engine) throws CommandException {
		try {
			Interpreter interpreter = new Interpreter(engine.getConsole());
			//pass variables
			setVariables(interpreter, engine);	
			//With using interpreter.run() calling exit will kill the JVM! There is no way to return to the shell.
			//interpreter.run();			
			interpreter.eval(expression);
		}
		catch (Exception e) {
			throw new CommandException(e);
		}
	}

	
	protected void bsh(FileObject file, Arguments args, Engine engine) throws CommandException, FileSystemException {
		InputStream input = null;
		try {
			input = file.getContent().getInputStream();
			bsh(input, args, engine);
		}
		catch (CommandException e) {
			throw e;
		}
		catch (FileSystemException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CommandException(e);
		}
		finally {
			if (input!=null) { 
				try {
					input.close();
				}
				catch (IOException e) {
					//ignore
				}	
			}
		}
	}
	
	protected void bsh(InputStream input, Arguments args, Engine engine) throws CommandException, FileSystemException {
		Reader reader = null;
		try {
			Interpreter interpreter = new Interpreter(engine.getConsole());
			//pass variables
			setVariables(interpreter, engine);
			
			interpreter.set("args", args);				
			
			reader = new InputStreamReader(input);
			interpreter.eval(reader);
		}
		catch (Exception e) {
			throw new CommandException(e);
		}
		finally {
			if (reader!=null) { 
				try {
					reader.close();
				}
				catch (IOException e) {
					//ignore
				}			
			}
		}
	}

	protected void setVariables(Interpreter interpreter, Engine engine) throws EvalError {
		//pass all variables
		Map vars = engine.getContext().getAll();
		Iterator iterator = vars.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
			Object value = vars.get(key);
			if (value!=null) {
				interpreter.set(key.toString(), value);
			}
		}
		//experimental:
		interpreter.set("engine", engine);
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
			result.addFlag(flag);
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
			else {
				Option option = (Option) args.getOptions().get(key);
				result.addOption(option);
			}
		}			
		
		return result;
	}

}
