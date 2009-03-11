package org.vfsutils.shell.mina1;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;

public class IoShell implements ConsoleInterface {

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
	
	protected Reader in;
	protected PrintStream out;
	
	protected EngineThread engineThread = null;
	
	public IoShell(InputStream in, OutputStream out) throws Exception {
		this.in = new InputStreamReader(in, "UTF-8");
		this.out = new PrintStream(out);
	}

	public void go() throws Exception {
		Engine engine = new Engine(this);
		this.engineThread = new EngineThread(engine);
		this.engineThread.start();
	}
	
	public void error(Object arg0) {
		out.println(arg0);
	}

	public PrintStream getErr() {
		return out;
	}

	public Reader getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}

	public void print(Object arg0) {
		out.print(arg0);
	}

	public void println(Object arg0) {
		out.println(arg0);
	}

}
