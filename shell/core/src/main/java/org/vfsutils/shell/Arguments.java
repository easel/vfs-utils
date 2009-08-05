package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Arguments {
	
	public abstract class Token {
		private String value;
		
		public Token(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public boolean equals(Object o) {
			if (o==null) return false;
			if (o instanceof Token) {
				Token other = (Token) o;
				if (!this.getClass().equals(o.getClass())) return false;
				if (value==null) return false;
				return value.equals(other.value);
			}
			return false;
		}
		
		public String toString() {
			return this.getValue();
		}

		public int hashCode() {
			String base = toString();
			return base.hashCode();
		}
		
		
	}
	
	public class Cmd extends Token {
		public Cmd(String value) {
			super(value);
		}
	}
	
	public class Argument extends Token {
		public Argument(String value) {
			super(value);
		}
	}
	
	public class Flag extends Token {
		public Flag(String value) {
			super(value);
		}
	}
	
	public class Option extends Token {
		private String name;
		
		public Option(String name, String value) {
			super(value);
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public boolean equals(Object o) {
			if (o==null) return false;
			if (o instanceof Option) {
				Option other = (Option) o;
				if (!this.getClass().equals(o.getClass())) return false;
				if (name==null || getValue()==null) return false;
				 
				return name.equals(other.name) && getValue().equals(other.getValue());
			}
			return false;
		}
		
		public  String toString() {
			return this.getName() + "=" + this.getValue();
		}
		
		public int hashCode() {
			String base = toString();
			return base.hashCode();
		}

	}
	
	public class TokenList extends ArrayList {		
	}
	
	public class ArgumentList extends ArrayList {
	}
	
	public class FlagSet extends HashSet {
	}
	
	public class OptionMap extends HashMap {		
	}

	protected TokenList allTokens = new TokenList();
	
	protected Cmd cmd = null;
	protected FlagSet flags = new FlagSet();
	protected OptionMap options = new OptionMap();
	protected ArgumentList arguments = new ArgumentList(); 
	
	public void setCmd(String cmd) {
		setCmd(new Cmd(cmd));
	}
	
	public void setCmd(Cmd cmd) {
		
		if (this.cmd==null || allTokens.isEmpty()) {
			this.allTokens.add(0, cmd);
		}
		else {
			//overwrite
			this.allTokens.set(0, cmd);
		}
		
		this.cmd = cmd;
	}
	
	public void addFlag(String flag) {
		addFlag(new Flag(flag));
	}
	
	public void addFlag(Flag flag) {
		if (this.flags.add(flag)) {
			this.allTokens.add(flag);
		}
	}
	
	public void addOption(String option, String value) {
		addOption(new Option(option, value));
	}
	
	public void addOption(Option option) {
		this.options.put(option.name, option);
		this.allTokens.add(option);
	}
	
	public void addArgument(String arg) {
		addArgument(new Argument(arg));
	}
	
	public void addArgument(Argument arg) {
		this.arguments.add(arg);
		this.allTokens.add(arg);
	}
	
	public boolean hasFlag(String flag) {
		return this.flags.contains(new Flag(flag));
	}
	
	public boolean hasFlag(char flag) {
		return this.flags.contains(new Flag(Character.toString(flag)));
	}
	
	public FlagSet getFlags() {
		return this.flags;
	}
	
	public boolean hasOption(String name) {
		return this.options.containsKey(name);
	}
	
	public String getOption(String name) {
		Option option = (Option) this.options.get(name);
		if (option==null) {
			return null;
		}
		else {
			return option.getValue();
		}
	}
	
	public String getOption(String name, String defaultValue) {
		String option = getOption(name);
		return (option==null?defaultValue:option);
	}
	
	public OptionMap getOptions() {
		return this.options;
	}
	
	public ArgumentList getArguments() {
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
			Argument arg = (Argument) this.arguments.get(index);
			return arg.getValue();
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
		if (this.cmd==null) {
			return null;
		}
		else {
			return this.cmd.getValue();
		}
	}
	
}