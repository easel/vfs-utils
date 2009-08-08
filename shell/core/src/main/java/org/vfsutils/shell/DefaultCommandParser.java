package org.vfsutils.shell;

import org.vfsutils.shell.Arguments.Argument;
import org.vfsutils.shell.Arguments.Cmd;
import org.vfsutils.shell.Arguments.Flag;
import org.vfsutils.shell.Arguments.Option;
import org.vfsutils.shell.Arguments.Token;

/**
 * Flags are one character and can be stuck together -ls is equal to -l -s
 * Long flags can be whole words --all
 * Options are long flags with a value, e.g. --block-size=1024  
 * 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class DefaultCommandParser extends CommandParser {

	public DefaultCommandParser() {
		super();
	}
	
	protected boolean isFlag(String token) {
		return token.startsWith("-") && !token.startsWith("--");
	}
	
	protected boolean isLongFlag(String token) {
		return token.startsWith("--") && (token.indexOf('=')==-1);
	}
	
	protected boolean isOption(String token) {
		return token.startsWith("--") && (token.indexOf('=')>-1);
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
	

}
