package org.vfsutils.shell.jline;

import jline.ArgumentCompletor.ArgumentDelimiter;
import jline.ArgumentCompletor.ArgumentList;
import jline.ArgumentCompletor.WhitespaceArgumentDelimiter;
import junit.framework.TestCase;

public class CustomArgumentDelimiterTest extends TestCase {
	

	public void testSanity() {
		//ArgumentDelimiter del = new WhitespaceArgumentDelimiter(); 
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		 
		String input = "whitespace";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 10);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(1, list.length);		
		assertEquals("whitespace", list[0]);
		
		assertEquals(10, args.getBufferPosition());
		assertEquals(10, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("whitespace", args.getCursorArgument());
		
		args = del.delimit(input, 5);
		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("whitespace", args.getCursorArgument());
		
		
		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 9));
		
	}
	
	public void testSimple() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "white space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 11);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(2, list.length);		
		assertEquals("white", list[0]);
		assertEquals("space", list[1]);
		
		assertEquals(11, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());
		
		args = del.delimit(input, 5);
		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white", args.getCursorArgument());
		
		args = del.delimit(input, 6);
		assertEquals(6, args.getBufferPosition());
		assertEquals(0, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());
		

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 10));
		
		assertTrue(del.isDelimiter(input, 5));
		
	}
	
	public void testCompatibility() {
		ArgumentDelimiter del = new WhitespaceArgumentDelimiter(); 
		 
		String input = "white space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 11);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(2, list.length);		
		assertEquals("white", list[0]);
		assertEquals("space", list[1]);
		
		assertEquals(11, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());
		
		args = del.delimit(input, 5);
		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white", args.getCursorArgument());
		
		args = del.delimit(input, 6);
		assertEquals(6, args.getBufferPosition());
		assertEquals(0, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());
		

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 10));
		
		assertTrue(del.isDelimiter(input, 5));
		
	}
	
	
	public void testEscapedWhitespace() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "white\\ space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 12);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(1, list.length);		
		assertEquals("white\\ space", list[0]);
		
		assertEquals(12, args.getBufferPosition());
		assertEquals(12, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());
		
		args = del.delimit(input, 5);

		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());
		
		args = del.delimit(input, 6);

		assertEquals(6, args.getBufferPosition());
		assertEquals(6, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());

		args = del.delimit(input, 7);

		assertEquals(7, args.getBufferPosition());
		assertEquals(7, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 7));
		assertFalse(del.isDelimiter(input, 11));
		
	}
	
	/* breaks!
	public void testCompatibility2() {
		ArgumentDelimiter del = new WhitespaceArgumentDelimiter();
		
		String input = "white\\ space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 12);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(1, list.length);		
		assertEquals("white\\ space", list[0]);
		
		assertEquals(12, args.getBufferPosition());
		assertEquals(12, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());
		
		args = del.delimit(input, 5);

		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());
		
		args = del.delimit(input, 6);

		assertEquals(6, args.getBufferPosition());
		assertEquals(6, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());

		args = del.delimit(input, 7);

		assertEquals(7, args.getBufferPosition());
		assertEquals(7, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ space", args.getCursorArgument());

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 7));
		assertFalse(del.isDelimiter(input, 11));
		
	}
	*/
	
	public void testQuotedWhitespace1() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "'white space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 12);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(1, list.length);		
		assertEquals("'white space", list[0]);
		
		assertEquals(12, args.getBufferPosition());
		assertEquals(12, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("'white space", args.getCursorArgument());
		
		args = del.delimit(input, 5);

		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("'white space", args.getCursorArgument());
		
		args = del.delimit(input, 6);

		assertEquals(6, args.getBufferPosition());
		assertEquals(6, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("'white space", args.getCursorArgument());

		args = del.delimit(input, 7);

		assertEquals(7, args.getBufferPosition());
		assertEquals(7, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("'white space", args.getCursorArgument());

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 7));
		assertFalse(del.isDelimiter(input, 11));
		
	}

	public void testQuotedWhitespace2() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "white\" space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 12);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(1, list.length);		
		assertEquals("white\" space", list[0]);
		
		assertEquals(12, args.getBufferPosition());
		assertEquals(12, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\" space", args.getCursorArgument());
		
		args = del.delimit(input, 5);

		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\" space", args.getCursorArgument());
		
		args = del.delimit(input, 6);

		assertEquals(6, args.getBufferPosition());
		assertEquals(6, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\" space", args.getCursorArgument());

		args = del.delimit(input, 7);

		assertEquals(7, args.getBufferPosition());
		assertEquals(7, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\" space", args.getCursorArgument());

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 7));
		assertFalse(del.isDelimiter(input, 11));
		
	}
	
	public void testWhitespaceAfterEscape() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "white\\  space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 13);
		//there should only be one argument
		String[] list = args.getArguments();
		assertEquals(2, list.length);		
		assertEquals("white\\ ", list[0]);
		assertEquals("space", list[1]);
		
		assertEquals(13, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());
		
		args = del.delimit(input, 5);
		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ ", args.getCursorArgument());
		
		args = del.delimit(input, 6);
		assertEquals(6, args.getBufferPosition());
		assertEquals(6, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ ", args.getCursorArgument());
		
		args = del.delimit(input, 7);
		assertEquals(7, args.getBufferPosition());
		assertEquals(7, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\ ", args.getCursorArgument());

		args = del.delimit(input, 8);
		assertEquals(8, args.getBufferPosition());
		assertEquals(0, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 8));
		assertFalse(del.isDelimiter(input, 12));
		
		assertTrue(del.isDelimiter(input, 7));
		
	}
	
	public void testDoubleEscape() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "white\\\\ space";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 13);
		
		String[] list = args.getArguments();
		assertEquals(2, list.length);		
		assertEquals("white\\\\", list[0]);
		assertEquals("space", list[1]);
		
		assertEquals(13, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());
		
		args = del.delimit(input, 5);

		assertEquals(5, args.getBufferPosition());
		assertEquals(5, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\\\", args.getCursorArgument());
		
		args = del.delimit(input, 6);

		assertEquals(6, args.getBufferPosition());
		assertEquals(6, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\\\", args.getCursorArgument());
		
		args = del.delimit(input, 7);

		assertEquals(7, args.getBufferPosition());
		assertEquals(7, args.getArgumentPosition());
		assertEquals(0, args.getCursorArgumentIndex());
		assertEquals("white\\\\", args.getCursorArgument());

		
		args = del.delimit(input, 8);

		assertEquals(8, args.getBufferPosition());
		assertEquals(0, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("space", args.getCursorArgument());

		//isDelimiter (can not ask for pos>=length)
		assertFalse(del.isDelimiter(input, 0));
		assertFalse(del.isDelimiter(input, 5));
		assertFalse(del.isDelimiter(input, 6));
		assertFalse(del.isDelimiter(input, 8));
		assertFalse(del.isDelimiter(input, 12));
		
		assertTrue(del.isDelimiter(input, 7));
		
	}
	
	public void testPartsplit() {
		ArgumentDelimiter del = new CustomArgumentDelimiter();
		
		String input = "white ";
		
		//first try with cursor at end
		ArgumentList args = del.delimit(input, 6);
		
		String[] list = args.getArguments();
		assertEquals(2, list.length);		
		assertEquals("white", list[0]);
		assertEquals("", list[1]);
		
		assertEquals(6, args.getBufferPosition());
		assertEquals(0, args.getArgumentPosition());
		assertEquals(1, args.getCursorArgumentIndex());
		assertEquals("", args.getCursorArgument());
		
	}
	
	
}
