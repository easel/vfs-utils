package org.vfsutils.shell.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;

public class CommandRunner implements ConsoleInterface, EngineRunner {

	protected ByteArrayOutputStream outBuffer = new ByteArrayOutputStream(2048);
	protected PrintStream out = new PrintStream(outBuffer);
	
	protected ByteArrayOutputStream errBuffer = new ByteArrayOutputStream(2048);
	protected PrintStream err = new PrintStream(errBuffer);
	
	protected Engine engine = null;
	
	
	public void startEngine(Engine engine) {
		this.engine = engine;		
	}
	
	public ShellResponse stopEngine() {
		return new ShellResponse("stopped","");
	}
	
	public ShellResponse handleInput(ShellRequest request) throws IOException {
		
		//clear for new output
		outBuffer.reset();
		errBuffer.reset();
		
		try {
			engine.handleCommand(request.getIn());
		}
		catch (Exception e) {
			return new ShellResponse(e.getMessage(), "");
		}
		
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
		return null;
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

}
