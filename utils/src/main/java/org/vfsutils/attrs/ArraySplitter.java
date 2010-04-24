package org.vfsutils.attrs;

import org.vfsutils.StringSplitter;

public class ArraySplitter extends StringSplitter {
	
	public ArraySplitter() {
		this.setDelimiterChars(new char[] {',',' ', '\t', '\r', '\n'});
		this.setKeepQuotesAndEscapeChars(true);
	}	
	
	public String[] split(String line) {
		if (line.charAt(0)=='[' && line.charAt(line.length()-1)==']'){
			line = line.substring(1, line.length()-1);
		}
		return super.split(line);
	}


}
