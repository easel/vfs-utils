package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Arguments {
	
	public class TokenList extends ArrayList {
		public String toString() {
			return asString(0);
		}
		
		public String asString(int startAt) {
			StringBuffer buffer = new StringBuffer();
			for (int i=startAt; i<allTokens.size(); i++) {
				if (buffer.length()>0) buffer.append(" ");
				buffer.append(escapeWhitespaceAndQuotes((String)allTokens.get(i)));
			}
			return buffer.toString();
		}
		
	}
	
	public class FlagSet extends HashSet {
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			Iterator iterator = this.iterator();
			
			while (iterator.hasNext()) {
				if (buffer.length()>0) buffer.append(" ");

				String flag = (String) iterator.next();
				
				if (flag.length()==1){
					buffer.append("-").append(flag);
				}
				else {
					buffer.append("--").append(flag);
				}
			}
			
			return buffer.toString();
		}
	}
	
	public class OptionMap extends HashMap {
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			Iterator iterator = this.keySet().iterator();
			while (iterator.hasNext()) {
				if (buffer.length()>0) buffer.append(" ");
				String key = (String) iterator.next();
				buffer.append("--").append(key).append("=").append(escapeWhitespaceAndQuotes((String)this.get(key)));
			}
			return buffer.toString();
		}
	}

	protected TokenList allTokens = new TokenList();
	
	protected String cmd = null;
	protected FlagSet flags = new FlagSet();
	protected OptionMap options = new OptionMap();
	protected TokenList arguments = new TokenList(); 
	
	public void addFlags(String flags) {
		//skip the - and add each flag separately
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
	
	public FlagSet getFlags() {
		return this.flags;
	}
	
	public boolean hasOption(String name) {
		return this.options.containsKey(name);
	}
	
	public String getOption(String name) {
		return (String) this.options.get(name);
	}
	
	public OptionMap getOptions() {
		return this.options;
	}
	
	public TokenList getArguments() {
		return this.arguments;
	}
	
	/**
	 * Return the argument at the given index, if the argument
	 * does not exist an empty string is returned.
	 * @param index
	 * @return
	 */
	public String getArgument(int index) {
		if (index<0 || index>=this.arguments.size()) {
			return "";
		}
		else {
			return (String) this.arguments.get(index);
		}
	}
	
	public int size() {
		return this.arguments.size();
	}
	
	public void assertSize(int required) throws IllegalArgumentException {
		if (this.size() < required) {
			throw new IllegalArgumentException("Not enough arguments");
		}
	}
	
	public TokenList getAllTokens() {
		return this.allTokens;
	}
	
	public boolean hasCmd() {
		return (this.cmd != null);
	}
	
	public String getCmd() {
		return this.cmd;
	}
	
	public String toString() {
		return allTokens.toString();
	}
	
	public String asString(int startAt) {
		return allTokens.asString(startAt);
	}
	
	
	protected String escapeWhitespaceAndQuotes(String input) {
		return escape(input, new char[] {' ', '\'', '"' });
	}
		
	protected String escape(String input, char[] matches) {
		StringBuffer buffer = new StringBuffer(input.length()+5);
		char[] chars = input.toCharArray();
		
		for (int i=0; i < chars.length; i++) {
			char c = chars[i];
			for (int j=0; j<matches.length; j++) {
				if (c==matches[j]) {
					buffer.append('\\');
				}
			}			
			buffer.append(c);
		}
		
		return buffer.toString();

	}
}