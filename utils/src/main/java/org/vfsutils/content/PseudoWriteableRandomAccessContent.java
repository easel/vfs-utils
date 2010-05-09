package org.vfsutils.content;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.util.RandomAccessMode;

/**
 * Class that offers a Writeable RandomAccessContent interface for 
 * file systems that do not support it natively
 * @author kleij - at - users.sourceforge.net
 *
 */
public class PseudoWriteableRandomAccessContent extends
		AbstractWriteableRandomAccessContent {


	private long filePointer = 0;
	
	private FileObject file;
	private OutputStream mos;
	private DataOutputStream dos;

	
	public PseudoWriteableRandomAccessContent(FileObject file, RandomAccessMode mode) {
		super(mode);
		this.file = file;
	}

	protected DataOutputStream getDataOutputStream() throws IOException {
		if (dos!=null) {
			return dos; 
		}

		this.mos = file.getContent().getOutputStream();
		//this.mos.skip(filePointer);		
		
		dos = new DataOutputStream(new FilterOutputStream(mos)
        {
			
            public void write(byte[] b, int off, int len) throws IOException {
				out.write(b, off, len);
				filePointer += len;
			}

			public void write(byte[] b) throws IOException {
				out.write(b);
				filePointer++;
			}

			public void write(int b) throws IOException {
				out.write(b);
				filePointer++;
			}

            public void close() throws IOException
            {
            	if (mos!=null) {
            		mos.close();
            		mos = null;
            	}
            }
        });
		
		return dos;
	}


	protected DataInputStream getDataInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() throws IOException {
		if (dos!=null) {
			try {
				dos.close();
			}
			catch (IOException e) {
				// ignore
			}
		}

	}

	public long getFilePointer() throws IOException {
		return filePointer;
	}

	public long length() throws IOException {
		return file.getContent().getSize();
	}

	public void seek(long pos) throws IOException {
		if (pos==filePointer) {
			return;
		}
		
		//TODO: handle cases where pos <> filePointer
		throw new IOException("Random access not supported");
		//filePointer = pos;
	}

}
