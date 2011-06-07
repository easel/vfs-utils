package org.vfsutils.content;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.vfs2.provider.AbstractRandomAccessStreamContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * Abstract class to facilitate writing, like AbstractRandomAccessStreamContent
 * @author kleij - at - users.sourceforge.net
 *
 */
public abstract class AbstractWriteableRandomAccessContent extends
		AbstractRandomAccessStreamContent {

	protected AbstractWriteableRandomAccessContent(RandomAccessMode mode) {
		super(mode);
	}
	
	protected abstract DataOutputStream getDataOutputStream() throws IOException;
	

	public void write(byte[] b, int off, int len) throws IOException {
		getDataOutputStream().write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		getDataOutputStream().write(b);
	}

	public void write(int b) throws IOException {
		getDataOutputStream().write(b);
	}

	public void writeBoolean(boolean v) throws IOException {
		getDataOutputStream().writeBoolean(v);
	}

	public void writeByte(int v) throws IOException {
		getDataOutputStream().writeByte(v);
	}

	public void writeBytes(String s) throws IOException {
		getDataOutputStream().writeBytes(s);
	}

	public void writeChar(int v) throws IOException {
		getDataOutputStream().writeChar(v);
	}

	public void writeChars(String s) throws IOException {
		getDataOutputStream().writeChars(s);
	}

	public void writeDouble(double v) throws IOException {
		getDataOutputStream().writeDouble(v);
	}

	public void writeFloat(float v) throws IOException {
		getDataOutputStream().writeFloat(v);
	}

	public void writeInt(int v) throws IOException {
		getDataOutputStream().writeInt(v);
	}

	public void writeLong(long v) throws IOException {
		getDataOutputStream().writeLong(v);
	}

	public void writeShort(int v) throws IOException {
		getDataOutputStream().writeShort(v);
	}

	public void writeUTF(String str) throws IOException {
		getDataOutputStream().writeUTF(str);
	}

	

}
