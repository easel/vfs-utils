package org.vfsutils.shell.jline;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import jline.ConsoleReader;

public class MaskingConsoleReader extends ConsoleReader {
	
	private boolean maskNext = false;
	private Character maskCharacter = new Character('*');
	
	public MaskingConsoleReader(InputStream in, Writer out) throws IOException {
		super(in, out);
	}

	/**
	 * If the maskNext property has been set the console will be masking the input
	 * using the specified character. Once the line is read the maskNext property 
	 * is reset and next read will be unmasked.
	 */
	public String readLine() throws IOException {
		
		if (!maskNext) {
			return super.readLine();
		}
		else {
			maskNext = false;
			return super.readLine(maskCharacter);				
		}
	}

	public boolean isMaskNext() {
		return maskNext;
	}

	public void setMaskNext(boolean maskNext) {
		this.maskNext = maskNext;
	}

	public Character getMaskCharacter() {
		return maskCharacter;
	}

	public void setMaskCharacter(Character maskCharacter) {
		this.maskCharacter = maskCharacter;
	}

}
