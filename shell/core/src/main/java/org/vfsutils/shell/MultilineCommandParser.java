package org.vfsutils.shell;

/**
 * Does not parse the line when the line ends with a backslash, but adds it to
 * an internal buffer. When the line ends with a double backslash the line is 
 * parsed at once
 * @author kleij -at- users.sourceforge.net
 *
 */
public class MultilineCommandParser extends DefaultCommandParser {

	StringBuffer history = new StringBuffer();

	public Arguments parse(String line) {
		
		if (line.endsWith("\\") && !line.endsWith("\\\\")) {
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
