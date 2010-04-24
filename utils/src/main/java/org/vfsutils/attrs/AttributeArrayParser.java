package org.vfsutils.attrs;

import java.util.Map;
import java.util.TreeMap;

import org.vfsutils.StringSplitter;

/**
 * Parses an array of attributes into a map
 * For example
 * [attr1=a, attr2=b, attr3="c,d", attr4="e=f"]
 * will become
 * ("attr1","a"),("attr2","b"),("attr3","c,d"), ("attr4","e=f") 
 * The following constructs will not work: [attr5=g,h , attr6=i=j] 
 * because ',' and '=' are delimiter characters and should be quoted 
 * or escaped: [attr5="g,h", attr6=i\=j].
 * 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class AttributeArrayParser {
	
	private ArraySplitter arraySplitter;
	private StringSplitter attrSplitter;
	
	public AttributeArrayParser() {
		this.arraySplitter = new ArraySplitter();
		this.attrSplitter = new StringSplitter();
		this.attrSplitter.setDelimiterChars(new char[] {'='});
	}
	
	public Map parse(String input){
		Map result = new TreeMap();
		String[] tokens = this.arraySplitter.split(input);
		for (int i=0; i<tokens.length; i++) {
			String token = tokens[i];
			String[]keyAndValue = attrSplitter.split(token);
			if (keyAndValue.length==2) {
				result.put(keyAndValue[0], keyAndValue[1]);
			}
			else {
				//TODO: log
				//ignore
			}
		}
		return result;
	}

}
