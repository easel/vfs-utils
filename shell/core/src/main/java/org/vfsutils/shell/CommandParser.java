package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Flags are one character and can be stuck together -ls is equal to -l -s
 * Long flags can be whole words --all
 * Options are long flags with a value, e.g. --block-size=1024  
 * 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class CommandParser {
	
	//TODO: these must move to Arguments since it also does the inverse (toString) or,
	//even better, the toString(Arguments) must move here
	protected boolean isFlag(String token) {
		return token.startsWith("-") && !token.startsWith("--");
	}
	
	protected boolean isLongFlag(String token) {
		return token.startsWith("--") && (token.indexOf('=')==-1);
	}
	
	protected boolean isOption(String token) {
		return token.startsWith("--") && (token.indexOf('=')>-1);
	}
	
	
	public Arguments parse(String line) {
		String[] cmd = split(line);
		return parse(cmd);
	}
	
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
	 * Splits a line in tokens at whitespace boundaries. Whitespace can be escaped by a backslash and 
	 * quoted blocks (single or double quotes) to avoid a split.
	 * @param line
	 * @return the tokens after the split at whitespace
	 */
	public String[] split(String line) {
		
		List parts = new ArrayList();
		
		char[] chars = line.toCharArray();
		
		StringBuffer part = new StringBuffer();
		
		boolean escaped = false;
		boolean inBlock = false;
		char blockChar = '0';
		
		char c;
		for (int i=0; i < chars.length; i++) {
			c = chars[i];
			//reset escape
			escaped = false;
			
			//if escaped, skip escape character
			if (c=='\\') {
				escaped = true;
				//skip to next
				i++;
				//stop if at end
				if (i>=chars.length) break;
				//reassign c
				c = chars[i];
			}
			
			if (c=='"' || c=='\'') {
				if (escaped) {
					part.append(c);
				}
				else if (inBlock && blockChar==c) {
					inBlock = false;
				}
				else if (inBlock) {
					part.append(c);
				}
				else {
					inBlock=true;
					blockChar=c;
				}
			}
			else if (c==' ' || c=='\n') {
				if (escaped || inBlock) {
					part.append(c);
				}
				else {
					//when space (and not escaped) split
					addPart(parts, part);
				}
			}
			else {
				part.append(c);
			}
		}
		
		//treat left overs
		if (part.length()>0) {
			addPart(parts, part);
		}
		
		String[] result = new String[parts.size()];
		return (String[]) parts.toArray(result);
	}

	protected void addPart(List parts, StringBuffer part) {
		
		if (part.length()==0) return;
		
		parts.add(part.toString());
		part.delete(0, part.length());
	}
	
	protected void setCmd(Arguments args, String cmd) {
		args.setCmd(cmd);
	}
	
	protected void addFlags(Arguments args, String flags) {
		//skip the - and add each flag separately
		for (int i=1; i<flags.length(); i++) {
			args.addFlag(String.valueOf(flags.charAt(i)));
		}
	}
	
	protected void addLongFlag(Arguments args, String longFlag) {
		//remove leading --
		args.addFlag(longFlag.substring(2));
	}
	
	protected void addOption(Arguments args, String option) {
		//skip the -- and split at =
		String key = option.substring(2, option.indexOf('='));
		String value = option.substring(option.indexOf('=')+1);
		args.addOption(key, value);
	}
	
	protected void addArgument(Arguments args, String arg) {
		args.addArgument(arg);
	}
	
	public String toString(Arguments args) {
		return toString(args, 0);
	}
	
	public String toString(Arguments args, int startAt) {
		return toString(args.getAllTokens(), startAt);
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
	
	public String toString(TokenList tokens) {
		return toString(tokens, 0);
	}
	
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
	 * Writes a command token to the buffer
	 * @param buffer
	 * @param cmd
	 */
	protected void append(StringBuffer buffer, Cmd cmd) {
		buffer.append(cmd.getValue());
	}
	
	/**
	 * Writes an argument token to the buffer
	 * @param buffer
	 * @param arg
	 */
	protected void append(StringBuffer buffer, Argument arg) {
		buffer.append(escapeWhitespaceAndQuotes(arg.getValue()));
	}
	
	/**
	 * Writes an flag token to the buffer as -f for short flags
	 * and as --flag for long flags
	 * @param buffer
	 * @param flag
	 */
	protected void append(StringBuffer buffer, Flag flag) {
		if (flag.getValue().length()==1){
			buffer.append("-").append(escapeWhitespaceAndQuotes(flag.getValue()));
		}
		else {
			buffer.append("--").append(escapeWhitespaceAndQuotes(flag.getValue()));
		}
	}
	
	/**
	 * Writes an option token to the buffer as --name=value
	 * @param buffer
	 * @param option
	 */
	protected void append(StringBuffer buffer, Option option) {
		buffer.append("--").append(option.getName()).append("=").append(escapeWhitespaceAndQuotes(option.getValue())); 
	}
	
	/**
	 * This method works as a dispatcher; since overloading does not work run-time a type inspection is done
	 * to choose the correct method to invoke. If you would add a new Token sub-type you must override this
	 * method.
	 * @param buffer
	 * @param token
	 */
	protected void append(StringBuffer buffer, Token token) {
		if (token instanceof Cmd) append(buffer, (Cmd)token);
		else if (token instanceof Argument) append(buffer, (Argument) token);
		else if (token instanceof Flag) append(buffer, (Flag) token);
		else if (token instanceof Option) append(buffer, (Option) token);
		else throw new IllegalArgumentException("Method append(StringBuffer, Token) should be extended to support class" + token.getClass());
	}
	
	/**
	 * Escapes whitespace, single and double quotes by prepending a backslash
	 * @param input
	 * @return
	 */
	protected String escapeWhitespaceAndQuotes(String input) {
		return escape(input, new char[] {' ', '\'', '"' });
	}
		
	/**
	 * Escapes the given characters by prepending a backslash
	 * @param input
	 * @param matches
	 * @return
	 */
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
	
	/**
	 * Unescapes whitespace, single and double quotes by removing the
	 * prepending backslash 
	 * @param input
	 * @return
	 */
	protected String unescapeWhiteSpaceAndQuotes(String input) {
		return unescape(input, new char[] {' ', '\'', '"' });
	}
	
	/**
	 * Unescapes the given characters by removing the prepending backslash
	 * @param input
	 * @param matches
	 * @return
	 */
	protected String unescape(String input, char[] matches) {
		StringBuffer buffer = new StringBuffer(input.length());
		char[] chars = input.toCharArray();
		
		charloop: for (int i=0; i < chars.length; i++) {
			char c = chars[i];
			if (c=='\\') {
				//just hope no one escapes #
				char next = (i==(chars.length-1)?'#':chars[i+1]);
				for (int j=0; j<matches.length; j++) {
					if (next==matches[j]) {
						continue charloop;
						
					}
				}				
			}			
			buffer.append(c);
		}
		
		return buffer.toString();

	} 
}
