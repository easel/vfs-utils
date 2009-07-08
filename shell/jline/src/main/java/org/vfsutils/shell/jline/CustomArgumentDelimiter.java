package org.vfsutils.shell.jline;

import java.util.ArrayList;
import java.util.List;

import org.vfsutils.shell.StringSplitter;

import jline.ArgumentCompletor.ArgumentDelimiter;
import jline.ArgumentCompletor.ArgumentList;

public class CustomArgumentDelimiter 
		extends StringSplitter implements ArgumentDelimiter {

	
	public ArgumentList delimit(String buffer, int pos) {
		List parts = new ArrayList();
		
		char[] chars = buffer.toCharArray();
		
		int token = -1;
		int posInToken = -1;
		
		StringBuffer part = new StringBuffer();
		
		boolean escaped = false;
		boolean quoted = false;
		//initialized on dummy value
		char quoteChar = '0';
		
		char c;
		for (int i=0; i < chars.length; i++) {
			c = chars[i];

			//check if position was reached
			if (i==pos) {
				//the current position is reached
				token = parts.size();
				posInToken = part.length();
			}

			
			if (isEscape(c)) {
				if (escaped) {
					//escaped escape
					escaped = false;
					part.append(c);
				}
				else {
					escaped = true;
					part.append(c);
				}
			}
			else if (isQuote(c)) {
				if (escaped) {
					//reset escape
					escaped = false;
					part.append(c);					
				}
				else if (quoted && quoteChar==c) {
					quoted = false;
					part.append(c);
				}
				else if (quoted) {
					part.append(c);
				}
				else {
					quoted=true;
					quoteChar=c;
					part.append(c);
				}
			}
			else if (isDelimiter(c)) {
				if (escaped || quoted) {
					escaped = false;
					part.append(c);
				}
				else {
					//when space (and not escaped) split
					addPart(parts, part);
				}
			}
			else {
				escaped = false;
				part.append(c);
			}
			
			
		}
		
		//treat left-overs (even when empty)		
		parts.add(part.toString());		
		
		if (chars.length==pos) {
			//the last position matches
			token = parts.size()-1;
			posInToken = ((String)parts.get(parts.size()-1)).length();
		}
		
		
		String[] result = new String[parts.size()];
		
		return new ArgumentList((String[]) parts.toArray(result), token, posInToken, pos);
	}

	public boolean isDelimiter(String buffer, int pos) {
		
		boolean isDelimiter = false;
		
		char[] chars = buffer.toCharArray();
	
		boolean escaped = false;
		boolean quoted = false;
		//initialized on dummy value
		char quoteChar = '0';
		
		char c;
		for (int i=0; i <= Math.min(chars.length-1, pos); i++) {
			c = chars[i];

			if (isEscape(c)) {
				if (escaped) {
					//escaped escape
					escaped = false;
					isDelimiter = false;
				}
				else {
					escaped = true;
					isDelimiter = false;
				}
			}
			else if (isQuote(c)) {
				if (escaped) {
					//reset escape
					escaped = false;
					isDelimiter = false;					
				}
				else if (quoted && quoteChar==c) {
					quoted = false;
					isDelimiter = false;
				}
				else if (quoted) {
					isDelimiter = false;
				}
				else {
					quoted=true;
					quoteChar=c;
					isDelimiter = false;
				}
			}
			else if (isDelimiter(c)) {
				if (escaped || quoted) {
					escaped = false;
					isDelimiter = false;
				}
				else {
					//when space (and not escaped) split
					isDelimiter = true;
				}
			}
			else {				
				escaped = false;				
				isDelimiter = false;
			}
			
		}		
		
	    return isDelimiter;
		
	}


}
