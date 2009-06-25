package org.vfsutils.shell.jline;

import org.vfsutils.shell.CommandParser;
import org.vfsutils.shell.DefaultCommandParser;

import jline.ArgumentCompletor.WhitespaceArgumentDelimiter;
import jline.ArgumentCompletor.ArgumentDelimiter;
import jline.ArgumentCompletor.ArgumentList;

public class CustomArgumentDelimiter extends WhitespaceArgumentDelimiter
		implements ArgumentDelimiter {

	public boolean isEscaped(String buffer, int pos) {
		if (pos <= 0) {
			return false;
		}

		for (int i = 0; (getEscapeChars() != null) && (i < getEscapeChars().length); i++) {
			if (buffer.charAt(pos-1) == getEscapeChars()[i]) {
				if (pos-2<0) {
					return true;
				}
				else {
					return !isEscaped(buffer, pos - 2); // escape escape
				}
			}
		}

		return false;
	}

	/*public boolean isQuoted(String buffer, int pos) {
		
		boolean escaped = false;
		boolean inGroup = false;
		//initialized on dummy value
		char groupingChar = '0';
		
		char c;
		for (int i=0; i < pos; i++) {
			c = buffer.charAt(i);
			//reset escape
			escaped = false;
			
			//if escaped, skip escape character
			if (c=='\\') {
				escaped = true;
				//skip to next
				i++;
				//stop if at end
				if (i>=pos) break;
				//reassign c
				c = buffer.charAt(i);
			}
			
			if (c=='"' || c=='\'') {
				if (escaped) {
					//do nothing
				}
				else if (inGroup && groupingChar==c) {
					inGroup = false;
				}
				else if (inGroup) {
					//do nothing
				}
				else {
					inGroup=true;
					groupingChar=c;
				}
			}
			
			System.out.println(pos + " " + inGroup);
		}
		
		return inGroup;
	}
	*/

}
