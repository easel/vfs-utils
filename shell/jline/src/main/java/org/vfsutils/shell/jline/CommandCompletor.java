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
		
		Iterator iterator = commands.iterator();
		while (iterator.hasNext()) {
			String cmd = (String) iterator.next();
			if (cmd.startsWith(buffer)) {
				candidates.add(cmd);
			}
		}
		
		return 0;
	}

}
