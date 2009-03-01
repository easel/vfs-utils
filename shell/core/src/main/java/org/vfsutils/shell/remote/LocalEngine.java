package org.vfsutils.shell.remote;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.Socket;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;

public class LocalEngine extends Engine {

	
	private class ReaderThread extends Thread {

		private InputStream in;
		
		public ReaderThread(InputStream in) {
			this.in = in;
		}
		
		public void run() {
			try {
				byte[] cbuf = new byte[2048] ;
				int read = -1;
				while ( (read=in.read(cbuf)) > -1 ) {
					getConsole().getOut().write(cbuf, 0, read);
					getConsole().getOut().flush();
				}
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private Socket socket;
	
	
	public LocalEngine(ConsoleInterface console) throws FileSystemException {
		super(console);
		try {
			socket = new Socket("localhost", 9123);
			//ReaderThread reader = new ReaderThread(socket.getInputStream());
			//reader.start();
		}
		catch (IOException e) {
			throw new FileSystemException(e);
		}
	}

	
	
	public boolean handleCommand(Arguments args) {
		
		try {
			
			InputStream in = socket.getInputStream();
			
			String msg = args.toString();
			if (!msg.endsWith("\n")) msg += "\n";
			socket.getOutputStream().write(msg.getBytes("UTF-8"));
			socket.getOutputStream().flush();		
			
			/*
			ByteArrayOutputStream inBuffer = new ByteArrayOutputStream(2048);
			byte[] cbuf = new byte[2048] ;
			int read = -1;
			while ( (read=in.read(cbuf)) > -1 ) {
				inBuffer.write(cbuf, 0, read);
			}
			*/
			
			BufferedReader bin = new BufferedReader( 
					new InputStreamReader(in, "UTF-8"));
			String line;
			 while ( (line = bin.readLine())!=null) {
				 if (line.trim().equals("#end#")){
					 break;
				 }
				 this.println(line);
			 }
			
			//this.print(inBuffer.toString("UTF-8"));
						
		}
		catch (IOException e) {
			error(e.getMessage());
			if (haltOnError) {
				return false;
			}
		}
		return true;
	}
	
	
	
	

}
