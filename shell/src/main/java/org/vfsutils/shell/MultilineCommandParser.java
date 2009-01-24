package org.vfsutils.shell;

public class MultilineCommandParser extends CommandParser {

	StringBuffer history = new StringBuffer();

	public Arguments parse(String line) {
		
		if (line.endsWith(" \\")) {
			history.append(line.substring(0, line.length()-1));
			return new Arguments();
		}
		else {
			if (history.length()==0) {
				return super.parse(line);
			}
			else {
				history.append(line);
				Arguments args = super.parse(history.toString());
				history.delete(0, history.length());
				return args;
			}
		}			
	}	
	
}
