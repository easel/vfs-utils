package org.vfsutils.shell;

import java.util.Iterator;

import org.vfsutils.shell.Arguments.Argument;
import org.vfsutils.shell.Arguments.ArgumentList;
import org.vfsutils.shell.Arguments.Cmd;
import org.vfsutils.shell.Arguments.Flag;
import org.vfsutils.shell.Arguments.FlagSet;
import org.vfsutils.shell.Arguments.Option;
import org.vfsutils.shell.Arguments.OptionMap;
import org.vfsutils.shell.Arguments.Token;
import org.vfsutils.shell.Arguments.TokenList;

/**
 * Parses commands.
 * First input line are split on whitespace. It supports whitespace escaping and
 * grouping using single or double quotes. 
 * Then the tokens are transformed into an Arguments object. First it will try
 * to determine whether the token is a short flag, a long flag or an option. If
 * it is neither of them the token is assumed to be a normal argument.
 * Subclasses need to define the representation of these tokens and support
 * parsing and writing the tokens.
 * 
 * @author kleij - at - users.sourceforge.net
 *
 */
public abstract class CommandParser extends StringSplitter {
	
	/**
	 * Tells whether the token is a short flag
	 * @param token
	 * @return true if the token is a short flag
	 */
	protected abstract boolean isFlag(String token);
	
	/**
	 * Tells whether the token is a long flag
	 * @param token
	 * @return true if the token is a long flag
	 */
	protected abstract boolean isLongFlag(String token);
	
	/**
	 * Tells whether the token is an option
	 * @param token
	 * @return
	 */
	protected abstract boolean isOption(String token);
	
	/**
	 * First splits the input using split(String) and then calls
	 * parse(String[])
	 * @param line the input line
	 * @return a filled-in Arguments object
	 */
	public Arguments parse(String line) {
		String[] cmd = split(line);
		return parse(cmd);
	}
	
	/**
	 * Transforms the list of strings to an Arguments object.
	 * @param cmd list of input strings
	 * @return a filled-in Argument object
	 */
	public Arguments parse(String[] cmd) {		
		
		Arguments args = new Arguments();
		
		if (cmd.length>0) { 

			args.setCmd(cmd[0]);
			
			//ignore the first entry
			for (int i=1; i<cmd.length; i++) {
				String token = cmd[i];
				
				if (isFlag(token)) {
					addFlags(args, token);
				}
				else if (isLongFlag(token)) {
					addLongFlag(args, token);
				}
				else if (isOption(token)) {
					addOption(args, token);
				}
				else {
					addArgument(args, token);
				}
			}

		}
		return args;
	}
	
	/**
	 * Sets the given string as a command on the Arguments object.
	 * This might involve string manipulation.
	 * @param args
	 * @param cmd
	 */
	protected abstract void setCmd(Arguments args, String cmd);
	

	/**
	 * Sets the given string as an argument on the Arguments object
	 * This might involve string manipulation.
	 * @param args
	 * @param arg
	 */
	protected abstract void addArgument(Arguments args, String arg);
	
	/**
	 * Sets the given string as a short flag on the Arguments object.
	 * This typically involves string manipulation.
	 * @param args
	 * @param flags
	 */
	protected abstract void addFlags(Arguments args, String flags);
	
	/**
	 * Sets the given string as a long flag on the Arguments object
	 * This typically involves string manipulation.
	 * @param args
	 * @param longFlag
	 */
	protected abstract void addLongFlag(Arguments args, String longFlag);
	
	/**
	 * Sets the given string as an option on the Arguments object
	 * This typically involves string manipulation.
	 * @param args
	 * @param option
	 */
	protected abstract void addOption(Arguments args, String option);
	
	/**
	 * Creates a string representation of the Arguments object
	 * @param args
	 * @return
	 */
	public String toString(Arguments args) {
		return toString(args, 0);
	}
	
	/**
	 * Creates a string representation of the Arguments object starting
	 * from the given index (skipping the first tokens) 
	 * @param args
	 * @param startAt
	 * @return
	 */
	public String toString(Arguments args, int startAt) {
		return toString(args.getAllTokens(), startAt);
	}
	

	/**
	 * Creates a string representation of the list of all
	 * tokens
	 * @param tokens
	 * @return
	 */
	public String toString(TokenList tokens) {
		return toString(tokens, 0);
	}
	
	/**
	 * Creates a string representation of the list of all
	 * tokens starting at the given index (skipping the 
	 * first tokens)
	 * @param tokens
	 * @param startAt
	 * @return
	 */
	public String toString(TokenList tokens, int startAt) {
		StringBuffer buffer = new StringBuffer();
		for (int i=startAt; i<tokens.size(); i++) {
			if (buffer.length()>0) buffer.append(" ");
			Token t = (Token)tokens.get(i);
			append(buffer, t);
			
		}
		return buffer.toString();
	}
	
	/**
	 * Creates a string representation of all of the tokens in the arguments
	 * starting from the given index. You can indicate that the result should
	 * be unescaped (the default is escaped).
	 * @param args the arguments object
	 * @param startAt the index within the list of tokens to start at 
	 * @param unescaped if true, whitespace and quotes are unescaped (default is false)
	 * @return
	 */
	public String toString(Arguments args, int startAt, boolean unescaped) {
		String result = toString(args, startAt);
		if (unescaped) {
			result = unescapeWhiteSpaceAndQuotes(result);
		}
		return result;
	}
	

	/**
	 * Creates a string representation of the list of arguments
	 * @param args
	 * @return
	 */
	public String toString(ArgumentList args) {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = args.iterator();
		
		while (iterator.hasNext()) {
			if (buffer.length()>0) buffer.append(" ");

			Argument arg = (Argument) iterator.next();
			append(buffer, arg);	
		}
		
		return buffer.toString();

	}
	
	/**
	 * Creates a string representation of the set of flags
	 * @param flags
	 * @return
	 */
	public String toString(FlagSet flags) {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = flags.iterator();
		
		while (iterator.hasNext()) {
			if (buffer.length()>0) buffer.append(" ");

			Flag flag = (Flag) iterator.next();
			append(buffer, flag);	
		}
		
		return buffer.toString();
	}
	
	/**
	 * Creates a string representation of the map of options
	 * @param options
	 * @return
	 */
	public String toString(OptionMap options) {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = options.keySet().iterator();
		while (iterator.hasNext()) {
			if (buffer.length()>0) buffer.append(" ");
			String key = (String) iterator.next();
			Option option = (Option) options.get(key);
			append(buffer, option);
		}
		return buffer.toString();
	}
	
	/**
	 * Writes a command token to the buffer
	 * @param buffer
	 * @param cmd
	 */
	protected abstract void append(StringBuffer buffer, Cmd cmd);
	
	/**
	 * Writes an argument token to the buffer
	 * @param buffer
	 * @param arg
	 */
	protected abstract void append(StringBuffer buffer, Argument arg);
	
	/**
	 * Writes an flag token to the buffer 
	 * @param buffer
	 * @param flag
	 */
	protected abstract void append(StringBuffer buffer, Flag flag);
	
	/**
	 * Writes an option token to the buffer
	 * @param buffer
	 * @param option
	 */
	protected abstract void append(StringBuffer buffer, Option option);
	
	/**
	 * This method works as a dispatcher; since overloading does not work run-time a type inspection is done
	 * to choose the correct method to invoke. If you would add a new Token sub-type you must override this
	 * method.
	 * @param buffer
	 * @param token
	 */
	protected abstract void append(StringBuffer buffer, Token token);
	
	
}
