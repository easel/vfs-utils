package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Arguments {
	
	protected List allTokens = new ArrayList();
	
	protected String cmd = null;
	protected Set flags = new HashSet();
	protected Map options = new HashMap();
	protected List arguments = new ArrayList(); 
	
	public void addFlags(String flags) {
		//skip the -
		for (int i=1; i<flags.length(); i++) {
			this.flags.add(String.valueOf(flags.charAt(i)));
		}
		this.allTokens.add(flags);
	}
	
	public void setCmd(String cmd) {
		this.cmd = cmd;
		this.allTokens.add(0, cmd);
	}
	
	public void addLongFlag(String longFlag) {
		//skip the --
		this.flags.add(longFlag.substring(2));
		this.allTokens.add(longFlag);
	}
	
	public void addOption(String option) {
		//skip the -- and split at =
		String key = option.substring(2, option.indexOf('='));
		String value = option.substring(option.indexOf('=')+1);
		this.options.put(key, value);
		this.allTokens.add(option);
	}
	
	public void addArgument(String value) {
		this.arguments.add(value);
		this.allTokens.add(value);
	}
	
	public boolean hasFlag(String flag) {
		return this.flags.contains(flag);
	}
	
	public Set getFlags() {
		return this.flags;
	}
	
	public boolean hasOption(String name) {
		return this.options.containsKey(name);
	}
	
	public String getOption(String name) {
		return (String) this.options.get(name);
	}
	
	public Map getOptions() {
		return this.options;
	}
	
	public List getArguments() {
		return this.arguments;
	}
	
	public String getArgument(int index) {
		return (String) this.arguments.get(index);
	}
	
	public int size() {
		return this.arguments.size();
	}
	
	public void assertSize(int required) throws IllegalArgumentException {
		if (this.size() < required) {
			throw new IllegalArgumentException("Not enough arguments");
		}
	}
	
	public String[] getAllTokens() {
		return (String[]) this.allTokens.toArray(new String[this.allTokens.size()]);
	}
	
	public boolean hasCmd() {
		return (this.cmd != null);
	}
	
	public String getCmd() {
		return this.cmd;
	}
	
	public String toString() {
		return asString(0);
	}
	
	public String asString(int startAt) {
		StringBuffer buffer = new StringBuffer();
		for (int i=startAt; i<allTokens.size(); i++) {
			if (buffer.length()>0) buffer.append(" ");
			buffer.append(allTokens.get(i));
		}
		return buffer.toString();
	}
	
}