package org.vfsutils.shell.sshd;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

import bsh.ConsoleInterface;

/**
 * Echos the characters that are read. Most SSH clients do not show the 
 * entered characters themselves.
 * @author kleij - at - users.sourceforge.net
 *
 */
public class EchoReader extends FilterReader {

	private ConsoleInterface console;
	
	/**
	 * The EchoReader needs a reader to hook into and a console
	 * to write the output to.
	 * @param reader
	 * @param console
	 */
	public EchoReader(Reader reader, ConsoleInterface console) {
		super(reader);
		this.console = console;
	}

/*	
	public int read() throws IOException {
		int read = super.read();
		out.print((char)read);
		out.flush();
		return read;
	}
*/
	public int read(char[] cbuf, int off, int len) throws IOException {
		int read = super.read(cbuf, off, len);
		
		//If an eol is received then print a newline
		if (read==1 && cbuf[0]=='\r') {
			//transform return to console newline
			console.println("");		
		}
		else if (read==1 && cbuf[0]=='\u007f') {
			//transform delete to backspace (for MAC)
			console.print('\b');
		}
		else if (read==1) {
			console.print(Character.toString(cbuf[0]));
		}
		else {
			console.print(new String(cbuf, off, len));
		}
		
		return read;
	}
	
	
	

}
