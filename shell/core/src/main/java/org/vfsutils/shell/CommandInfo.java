package org.vfsutils.shell;

public class CommandInfo {
	
	private String description;
	private String usage;
	
	public CommandInfo(String description, String usage) {
		super();
		this.description = description;
		this.usage = usage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUsage() {
		return usage;
	}
	
	public void setUsage(String usage) {
		this.usage = usage;
	}
	
}
