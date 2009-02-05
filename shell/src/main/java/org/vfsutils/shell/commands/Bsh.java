package org.vfsutils.shell.commands;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

import bsh.EvalError;
import bsh.Interpreter;

public class Bsh extends AbstractCommand {

	public Bsh() {
		super("bsh", new CommandInfo("Execute a bsh file or expression", "-f <path>| <expression>"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
		
		try {
			
			args.assertSize(1);
			
			Interpreter interpreter = new Interpreter(engine.getConsole());
			
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
			
			if (args.hasFlag("f")) {
				FileObject file = engine.pathToFile(args.getArgument(0));
				
				if (!file.exists()) {
		        	throw new IllegalArgumentException("File does not exist " + engine.toString(file));
		        }
				
				//copy the arguments
				Arguments largs = copyArgs(args);
				interpreter.set("args", largs);				
				
				bsh(file, interpreter, engine);
			}
			else {
				//concat all arguments
				String script = args.asString(1);				
				bsh(script, interpreter, engine);
			}
			
		}
		catch (Exception e) {
			throw new FileSystemException(e);
		}

	}
	
	protected void bsh(String expression, Interpreter interpreter, Engine engine) throws FileSystemException {
		try {
				
			//With using interpreter.run() calling exit will kill the JVM! There is no way to return to the shell.
			//interpreter.run();			
			interpreter.eval(expression);
		}
		catch (Exception e) {
			throw new FileSystemException(e);
		}
	}

	protected void bsh(FileObject file, Interpreter interpreter, Engine engine) throws FileSystemException {
		Reader reader = null;
		try {
			reader = new InputStreamReader(file.getContent().getInputStream());
			interpreter.eval(reader);
		}
		catch (Exception e) {
			throw new FileSystemException(e);
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
			if (flag.equals("f")) {
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

}
