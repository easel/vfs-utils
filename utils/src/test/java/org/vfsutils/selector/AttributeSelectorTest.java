package org.vfsutils.selector;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

public class AttributeSelectorTest extends TestCase {

	public void testIncludeSize() {
		AttributeSelector sel = new AttributeSelector();
		
		assertTrue(sel.includeSize(0, "lte0"));
		assertTrue(sel.includeSize(-1, "lte0"));
		try {
			sel.includeSize(-1, "lte-1k");
			fail("invalid operator expected");
		}
		catch (IllegalArgumentException e) {}
		
		try {
			sel.includeSize(-1, "zz1G");
			fail("invalid operator expected");
		}
		catch (IllegalArgumentException e) {}
		
		try {
			sel.includeSize(-1, "gt1Z");
			fail("invalid modifier expected");
		}
		catch (IllegalArgumentException e) {}
		
		
		assertTrue(sel.includeSize(1234, "eq1234"));
		assertTrue(sel.includeSize(1234, "gt1024"));
		assertTrue(sel.includeSize(1234, "gt1k"));
		assertTrue(sel.includeSize(1234, "lt1m"));
		assertTrue(sel.includeSize(1234, "lt1g"));
		
		assertTrue(sel.includeSize(12341234, "neq10241024"));
		assertTrue(sel.includeSize(12341234, "gte10241024"));
		assertTrue(sel.includeSize(12341234, "gte1024k"));
		assertTrue(sel.includeSize(12341234, "gte1m"));
		assertTrue(sel.includeSize(12341234, "lte1g"));
		
		assertTrue(sel.includeSize(123412341234l, "neq102410241024"));
		assertTrue(sel.includeSize(123412341234l, "gt102410241024"));
		assertTrue(sel.includeSize(123412341234l, "gte1024K"));
		assertTrue(sel.includeSize(123412341234l, "gt1M"));
		assertTrue(sel.includeSize(123412341234l, "gte1G"));
		
	}

	
	public void testIncludeAge() {
		AttributeSelector sel = new AttributeSelector();
		
		long s12 = 1000 * 12;
		long w3 = 1000 * 60 * 60 * 24 * 7 * 3;
		
		assertTrue(sel.includeAge(s12, "gte12000ms"));
		assertTrue(sel.includeAge(s12, "gt11s"));
		assertTrue(sel.includeAge(s12, "lt1h"));
		assertTrue(sel.includeAge(s12, "lt1"));
		
		assertTrue(sel.includeAge(w3, "lte3w"));
		assertTrue(sel.includeAge(w3, "gte3w"));
		assertTrue(sel.includeAge(w3, "gt20d"));
		
		
	}
	
	public void testIncludeAttributes() {
		AttributeSelector sel = new AttributeSelector();
		Map values = new TreeMap();
		values.put("attr1", "12");
		values.put("attr2", "13");
		values.put("attr3", new Integer(14));
		
		assertTrue(sel.includeAttributes(values, "[attr1==12, attr2!=12]"));
		assertTrue(sel.includeAttributes(values, "[attr1==12, attr2==13]"));
		assertFalse(sel.includeAttributes(values, "[attr1!=12, attr2==13]"));
		assertFalse(sel.includeAttributes(values, "[attr1==12, attr2!=13]"));
		
		assertTrue(sel.includeAttributes(values, "[attr1==12 , attr2<>12]"));
		
		assertTrue(sel.includeAttributes(values, "[attr3<>12 , , attr3==14]"));
		
		assertTrue(sel.includeAttributes(values, "attr3==14"));
		
	}
}
