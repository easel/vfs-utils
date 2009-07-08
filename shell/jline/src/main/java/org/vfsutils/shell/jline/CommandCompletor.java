package org.vfsutils.shell.jline;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vfsutils.shell.Engine;

import jline.Completor;

public class CommandCompletor implements Completor {
	
	protected Engine engine;

	public CommandCompletor(Engine engine) {
		this.engine = engine;
	}


	public int complete(String buffer, int cursor, List candidates) {
		Set commands = engine.getCommandRegistry().getAllCommands();
		
		String workBuffer = buffer;
		//if the cursor is before the end of the buffer
		//then only regard the part until the cursor
		if (cursor<buffer.length()) {
			workBuffer = buffer.substring(0, cursor);
		}
		
		Iterator iterator = commands.iterator();
		while (iterator.hasNext()) {
			String cmd = (String) iterator.next();
			if (cmd.startsWith(workBuffer)) {
				candidates.add(cmd);
			}
		}
		
		return 0;
	}

}
