package org.vfsutils.shell;

import junit.framework.TestCase;

public class StringSplitterTest extends TestCase {
	
	private StringSplitter splitter;
	
	protected void setUp() throws Exception {
		splitter = new StringSplitter();
	}
	
	public void testJoin() {
		splitter.setDelimiterChars(new char[] {' '});
		splitter.setQuoteChars(new char[] {'\'','"'});
		char[] expected = new char[]{' ', '\'', '"'};
		char[] actual = splitter.joinDelimitersAndQuotes();
		
		assertEquals(expected.length, actual.length);
		
		for (int i=0; i<expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
		
	}
	
	public void testSanity() {
		String line = "hello";
		
		String[] result = splitter.split(line);
		assertEquals(1, result.length);
		assertEquals("hello", result[0]);
	}
	
	public void testSimple() {
		String line = "hello world";
		
		String[] result = splitter.split(line);
		assertEquals(2, result.length);
		assertEquals("hello", result[0]);
		assertEquals("world", result[1]);
	}
	
	public void testTrailingSpaces() {
		String line = "  hello ";
		
		String[] result = splitter.split(line);
		assertEquals(1, result.length);
		assertEquals("hello", result[0]);
	}
	

	public void testDoubleSpaces() {
		String line = " hello  world   ";
		
		String[] result = splitter.split(line);
		assertEquals(2, result.length);
		assertEquals("hello", result[0]);
		assertEquals("world", result[1]);
	}
	
	public void testSplit1() {
		String line = "this\\ has 'two parts'";
		
		String[] result = splitter.split(line);
		assertEquals(2, result.length);
		assertEquals("this has", result[0]);
		assertEquals("two parts", result[1]);
	}

	public void testSplit2() {
		String line = "'this\\ has' ' ' \"'three parts'\"";
		
		String[] result = splitter.split(line);
		assertEquals(3, result.length);
		assertEquals("this has", result[0]);
		assertEquals(" ", result[1]);
		assertEquals("'three parts'", result[2]);
	}
	
	public void testSplit3() {
		String line = "'this\\ \"has' \" \"  \"\'three parts\'\"";
		
		String[] result = splitter.split(line);
		assertEquals(3, result.length);
		assertEquals("this \"has", result[0]);
		assertEquals(" ", result[1]);
		assertEquals("'three parts'", result[2]);
	}
	
	public void testRemoveQuotesAndEscapes() {
		assertEquals("hello world/don't/one", splitter.removeQuotesAndEscapes("hello\\ world/don\\'t/one"));
		assertEquals("hello world/don't/one", splitter.removeQuotesAndEscapes("\"hello world/don't/one\""));
		assertEquals("hello  world/ don't/one  ", splitter.removeQuotesAndEscapes("\"hello  world/ don't/one  "));
	}
	
	public void testUnescapeWhitespaceAndQuotes() {
		assertEquals("hello world/don't/one", splitter.unescapeWhiteSpaceAndQuotes("hello\\ world/don\\'t/one"));
		assertEquals("\"hello world/don't/one\"", splitter.unescapeWhiteSpaceAndQuotes("\"hello world/don't/one\""));
	}
	
	public void testNormalize() {
		char[] input = new char[]{'h', 'a', 'l', '\b', '\b', 'e', 'l', 'l', 'o', '\t', 'w', 'o', 'r', 'l', 't', '\u007f', 'd'};
		
		String line = new String(input);
		assertEquals("hello\tworld", splitter.normalize(line));

		String[] result = splitter.split(line);
		assertEquals(2, result.length);
		assertEquals("hello", result[0]);
		assertEquals("world", result[1]);
	}
	

}
