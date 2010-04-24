package org.vfsutils.attrs;

import java.util.Map;

import junit.framework.TestCase;

public class AttributeArrayParserTest extends TestCase {
	
	public void testParse1() {
		String input="[attr1=a, attr2=b, attr3=\"c,d\", attr4='e=f', attr5=g\\,h, attr6=i\\=j]";
		AttributeArrayParser p = new AttributeArrayParser();
		Map result = p.parse(input);
		assertEquals("a", result.get("attr1"));
		assertEquals("b", result.get("attr2"));
		assertEquals("c,d", result.get("attr3"));
		assertEquals("e=f", result.get("attr4"));
		assertEquals("g,h", result.get("attr5"));
		assertEquals("i=j", result.get("attr6"));
	}

}
