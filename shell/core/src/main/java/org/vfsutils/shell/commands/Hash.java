package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Hash extends AbstractCommand {

	public Hash() {
		super("hash", "Generates hash code", "<string>+ --checksum=<code>");
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(1);
		
		Integer checksum = null;
		if (args.hasOption("checksum")) {
			try {
				checksum = Integer.valueOf(args.getOption("checksum"));
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid checksum given");
			}
		}
		
		for (int i=0; i<args.size(); i++) {
			hash(args.getArgument(i), checksum, engine);
		}
	}
	
	
	public void hash(String input, Integer checksum, Engine engine) {
		engine.println(input);
		int hashCode = input.hashCode();
		engine.println("Hash: " + Integer.toString(hashCode));
		if (checksum!=null) {
			engine.println("Checksum is " + (hashCode==checksum.intValue()?"equal":"different"));
		}		
	}

}
