package org.vfsutils.shell.commands;

import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.CommandRegistry;

public abstract class AbstractCommand implements CommandProvider {

	protected String cmd;
	protected CommandInfo info;
	
	public AbstractCommand(String cmd, CommandInfo info) {
		this.cmd = cmd;
		this.info = info;
	}
	
	public AbstractCommand(String cmd, String description, String usage) {
		this(cmd, new CommandInfo(description, usage));
	}
		
	public void setCommand(String cmd) {
		this.cmd = cmd;
	}
	
	public String getCommand() {
		return this.cmd;
	}
		
	public void setDescription(String description) {
		this.info.setDescription(description);
	}
	
	public String getDescription() {
		return this.info.getDescription();
	}
	
	public void setUsage(String usage) {
		this.info.setUsage(usage);
	}
	
	public String getUsage() {
		return this.info.getUsage();
	}	
		
	public void register(CommandRegistry reg) {
		reg.registerCommand(this.cmd, this);
	}

	public void unregister(CommandRegistry reg) {
		reg.unregisterCommand(this.cmd, this);
	}

}
