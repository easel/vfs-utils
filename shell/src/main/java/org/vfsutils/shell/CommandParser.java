package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses commands according to a mini DSL.
 * Flags are one character and can be stuck together -ls is equal to -l -s
 * Long flags can be whole words --all
 * Options are long flags with a value, e.g. --block-size=1024  
 * Examples:
 * cp source destination does 'Copies the source to the destination.'
 * pushd [dir] [-n] 
 *      'pushes the current directory on the stack and changes to dir' 
 * 		where n: 'only modifies the stack' 
 * 		examples {pushd, pushd .., pushd /tmp -n}
 * cd [dir|~i] where dir: 'destination' and i: 'index in directory stack' examples {cd /tmp, cd ~2}
 * ls [-ls] [dir]
 * xx --block-size=1024
 * @author kleij - at - users.sourceforge.net
 *
 */
public class CommandParser {
	
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
		
		args.allTokens = cmd;
		
		if (cmd.length>0) { 

			args.cmd = cmd[0];
			
			//ignore the first entry
			for (int i=1; i<cmd.length; i++) {
				String token = cmd[i];
				
				if (isFlag(token)) {
					args.addFlags(token);
				}
				else if (isLongFlag(token)) {
					args.addLongFlag(token);
				}
				else if (isOption(token)) {
					args.addOption(token);
				}
				else {
					args.addArgument(token);
				}
			}

		}
		return args;
	}
	
	/**
	 * Splits a line in tokens at whitespace boundaries. Whitespace can be escaped by a backslash to
	 * avoid a split.
	 * @param line
	 * @return the tokens after the split at whitespace
	 */
	public String[] split(String line) {
		
		List parts = new ArrayList();
		
		char[] chars = line.toCharArray();
		
		StringBuffer part = new StringBuffer();
		
		for (int i=0; i < chars.length; i++) {
			char c = chars[i];
			
			if (c=='\\') {
				//when escaped whitespace parse next character
				if (i+1<chars.length && (chars[i+1]==' ' || chars[i+1]=='\n')) {
					char c2 = chars[++i];
					part.append(c2);
				}
				else {
					part.append(c);
				}
			}
			else if (c==' ' || c=='\n') {
				//when space (and not escaped) split
				parts.add(part.toString());
				part.delete(0, part.length());
			}
			else {
				part.append(c);
			}
		}
		
		if (part.length()>0) {
			parts.add(part.toString());
		}
		
		String[] result = new String[parts.size()];
		return (String[]) parts.toArray(result);
	}

}
