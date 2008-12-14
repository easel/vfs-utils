package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Arguments {
	
	protected String[] allTokens;
	protected String cmd = null;
	
	protected Set flags = new HashSet();
	protected Map options = new HashMap();
	protected List arguments = new ArrayList(); 
	
	public void addFlags(String flags) {
		//skip the -
		for (int i=1; i<flags.length(); i++) {
			this.flags.add(String.valueOf(flags.charAt(i)));
		}
	}
	
	public void addLongFlag(String longFlag) {
		//skip the --
		this.flags.add(longFlag.substring(2));
	}
	
	public void addOption(String option) {
		//skip the -- and split at =
		String key = option.substring(2, option.indexOf('='));
		String value = option.substring(option.indexOf('=')+1);
		this.options.put(key, value);
	}
	
	public void addArgument(String value) {
		this.arguments.add(value);
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
	
	public void setAllTokens(String[] tokens) {
		this.allTokens = tokens;
	}
	
	public String[] getAllTokens() {
		return this.allTokens;
	}
	
	public boolean hasCmd() {
		return (this.cmd != null);
	}
	
	public String getCmd() {
		return this.cmd;
	}
	
}