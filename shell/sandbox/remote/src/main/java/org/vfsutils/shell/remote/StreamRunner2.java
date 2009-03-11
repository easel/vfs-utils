package org.vfsutils.shell.remote;

import java.io.ByteArrayOutputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintStream;
import java.io.Reader;

import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;


public class StreamRunner2 implements EngineRunner {

	protected class BlockingReader extends FilterReader {
		
		public BlockingReader(Reader reader) {
			super(reader);
		}

		public int read() throws IOException {
			
			//abort
			if (stopRequested) {
				//System.out.println("read aborted 0");
				throw new IOException("read aborted");
			}
			
			if (!this.in.ready()) {
				out.flush();
				err.flush();
				
				System.out.println("read: " + outBuffer.toString());
				
				//there is no input, the output can be sent now
				waitForOutput.release();
				
				//wait until new input arrives
				waitForInput.acquire();

				//abort
				if (stopRequested) {
					//System.out.println("read aborted 1");
					throw new IOException("read aborted");
				}
				
			}
			//System.out.println("reading");

			//do a normal read
			int read = super.read();

			//abort
			if (stopRequested) {
				//System.out.println("read aborted 3");
				throw new IOException("read aborted");
			}
			
			return read;
		}

		public int read(char[] cbuf, int off, int len) throws IOException {
			
			//abort
			if (stopRequested) {
				//System.out.println("read aborted 0");
				throw new IOException("read aborted");
			}
			
			if (!this.in.ready()) {
			
				out.flush();
				err.flush();
				
				
				System.out.println("readbuf: " + outBuffer.toString());
				
				
				//there is no input, the output can be sent now
				waitForOutput.release();
				
				//wait until new input arrives
				waitForInput.acquire();
				
				//abort
				if (stopRequested) {
					//System.out.println("read aborted 1");
					throw new IOException("read aborted");
				}
			}

			//System.out.println("reading buf");
			int read = super.read(cbuf, off, len);
			
			//abort
			if (stopRequested) {
				//System.out.println("read aborted 3");
				throw new IOException("read aborted");
			}
			
			return read;
		}
	}
	
	protected class EngineThread extends Thread {
		
		private Engine engine;
		
		public EngineThread(Engine engine) {
			this.engine = engine;
		}
		
		public void run() {
			
			try {
				engine.go();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void requestStop() {
			engine.stopOnNext();
		}
		
	}

	protected class Semaphore {
		private String name;
		private int count;

		public Semaphore(String name, int n) {
			this.name = name;
			this.count = n;
		}

		public synchronized void acquire() {
			while (count == 0) {
				try {
					//System.out.println("acquire " + name + " " + count + ": waiting");
					wait();
					System.out.println("acquire " + name + " " + count + ": continue");
				} catch (InterruptedException e) {
					// keep trying
				}
			}
			count--;
			//System.out.println("acquire " + name + " " + count);
		}

		public synchronized void release() {
			count++;
			System.out.println("release " + name + " " + count);
			notify(); // alert a thread that's blocking on this semaphore
		}
	}

	
	protected Semaphore waitForInput = new Semaphore("wairForInput", 0);
	protected Semaphore waitForOutput = new Semaphore("waitForOutput", 0);
	protected boolean stopRequested = false;
	
	protected PipedWriter inBuffer = new PipedWriter();
	protected FilterReader reader;
	
	protected ByteArrayOutputStream outBuffer = new ByteArrayOutputStream(2048);
	protected PrintStream out = new PrintStream(outBuffer);
	
	protected ByteArrayOutputStream errBuffer = new ByteArrayOutputStream(2048);
	protected PrintStream err = new PrintStream(errBuffer);
	
	protected EngineThread engineThread = null;
	
	public StreamRunner2() throws IOException {
		PipedReader pipedReader = new PipedReader(inBuffer);
		this.reader = new BlockingReader(pipedReader);
	}
	
	public void startEngine(Engine engine) {
		engineThread = new EngineThread(engine);		
		engineThread.start();		
	}
	
	public ShellResponse stopEngine() {
		
		if (!engineThread.isAlive()) {
			return new ShellResponse("", "Engine has stopped");
		}
		
		//System.out.println("stopping");
		
		ShellResponse result = new ShellResponse();

		try {
			//clear for new output
			outBuffer.reset();
			errBuffer.reset();
			
			//make the engine stop its loop
			engineThread.requestStop();
			
			//make the reader fail
			stopRequested = true;
			
			//release if waiting for lock
			waitForInput.release();
			
			//release if waiting for input 
			inBuffer.close();			
			
			//wait for the engine to exit
			engineThread.join(5000);

			//send the remaining output
			errBuffer.flush();
			outBuffer.flush();
			
			result.setErr(errBuffer.toString());
			result .setOut(outBuffer.toString());
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	
	public ShellResponse handleInput(ShellRequest request) throws IOException {

		if (!engineThread.isAlive()) {
			return new ShellResponse("", "Engine has stopped");
		}
		
		String input = request.getIn();
		
		if (input==null) {
			//handle null input
			return new ShellResponse("", "Invalid input");
		}
		else if (input.equals("exit") || input.equals("bye") || input.equals("quite")) {
			//intercept stopping to avoid deadlock
			return stopEngine();
		}
		
		//System.out.println(input);
		
		//clear for new output
		outBuffer.reset();
		errBuffer.reset();
		
		//insert the input in the reader
		//be sure to add a newline because reading is done with lines
		inBuffer.write(input.endsWith("\n")?input:input + "\n");
		inBuffer.flush();
				
		//tell the reader there is new input
		waitForInput.release();
				
		//wait for output
		waitForOutput.acquire();
		
		//handle output
		err.flush();
		out.flush();
		
		ShellResponse result = new ShellResponse(outBuffer.toString(), errBuffer.toString());
		
		//System.out.println("returning result");
		return result;
	}
	
	public void error(Object arg0) {
		this.err.println(arg0);
	}

	public PrintStream getErr() {
		return this.err;
	}

	public Reader getIn() {
		return this.reader;
	}

	public PrintStream getOut() {
		return this.out;
	}

	public void print(Object arg0) {
		this.out.print(arg0);		
	}

	public void println(Object arg0) {
		this.out.println(arg0);
	}
	
	public static void main(String[] args)  throws Exception {
		StreamRunner2 runner = new StreamRunner2();
		Engine engine = new Engine(runner);
		
		runner.startEngine(engine);				
		
		System.out.println(runner.handleInput(new ShellRequest("pwd")));
		System.out.println(runner.handleInput(new ShellRequest("ls")));
		System.out.println(runner.handleInput(new ShellRequest("cd ..")));
		System.out.println(runner.handleInput(new ShellRequest("cd d:/temp")));
		System.out.println(runner.handleInput(new ShellRequest("open -up zip://d:/temp/rules.zip")));
		System.out.println(runner.handleInput(new ShellRequest("usr")));
		System.out.println(runner.handleInput(new ShellRequest("pwd")));
		System.out.println(runner.handleInput(new ShellRequest("close")));
		System.out.println(runner.handleInput(new ShellRequest("exit")));

		System.out.println(runner.handleInput(new ShellRequest("close")));
		System.out.println(runner.stopEngine());
	}
	

}
