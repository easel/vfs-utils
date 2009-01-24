package org.vfsutils.shell;

import java.util.ArrayList;
import java.util.List;

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

	private void addPart(List parts, StringBuffer part) {
		
		if (part.length()==0) return;
		
		parts.add(part.toString());
		part.delete(0, part.length());
	}
	
}
