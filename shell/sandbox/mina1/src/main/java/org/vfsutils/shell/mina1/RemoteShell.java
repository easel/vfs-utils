package org.vfsutils.shell.mina1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Reader;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import bsh.ConsoleInterface;

public class RemoteShell implements ConsoleInterface {

	protected class IoSessionOutputStream extends OutputStream {
		
		private WriteFuture lastWrite = null;
		
		public IoSessionOutputStream() {}

		public void write(byte[] b, int off, int len) throws IOException {
			ioSession.write(new String(b, off, len, "UTF-8"));
		}

		public void write(byte[] b) throws IOException {
			ioSession.write(new String(b, "UTF-8"));
		}

		public void write(int b) throws IOException {
			write(new byte[] {(byte)b});
		}
		
		public synchronized void close() throws IOException {
			lastWrite = null;			
		}

		public synchronized void flush() throws IOException {
			if (lastWrite!=null) {
				lastWrite.join();
			}
		}
	}
	
	protected IoSession ioSession;
	
	protected PrintStream out = new PrintStream(new IoSessionOutputStream());
	protected PrintStream err = out;
	
	protected PipedWriter inBuffer = new PipedWriter();
	protected PipedReader reader;
	
	public RemoteShell(IoSession ioSession) throws IOException {
		this.ioSession = ioSession;
		 reader = new PipedReader(inBuffer);
	}
	
	public void error(Object arg0) {
		this.ioSession.write(arg0);

	}

	public PrintStream getErr() {
		return err;		
	}

	public Reader getIn() {
		return reader;
	}

	public PrintStream getOut() {
		return out;
	}

	public void print(Object arg0) {
		this.ioSession.write(arg0);

	}

	public void println(Object arg0) {
		this.ioSession.write(arg0);
		this.ioSession.write("\n");
	}

}
