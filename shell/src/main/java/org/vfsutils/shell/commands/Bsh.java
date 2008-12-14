package org.vfsutils.shell.commands;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

import bsh.Interpreter;

public class Bsh extends AbstractCommand {

	public Bsh() {
		super("bsh", new CommandInfo("Execute a bsh expression", "<expression>"));
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
			
			//With using interpreter.run() calling exit will kill the JVM! There is no way to return to the shell.
			//interpreter.run();
			
			//concat all arguments
			String script = "";
			String[] allTokens = args.getAllTokens();
			for (int i=1; i< allTokens.length; i++) {
				script += allTokens[i] + " ";
			}
			
			interpreter.eval(script);
		}
		catch (Exception e) {
			throw new FileSystemException(e);
		}

	}

}
