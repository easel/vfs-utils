package org.vfsutils.shell.jline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReaderInputStream;

import org.apache.commons.vfs2.FileSystemException;
import org.vfsutils.shell.Engine;

public class Shell extends org.vfsutils.shell.Shell {

	protected String maskTrigger = "password > ";
	
	protected MaskingConsoleReader consoleReader;
	
	public Shell(InputStream in) throws FileSystemException, IOException {
		super();
		this.engine = new Engine(this);
		this.consoleReader = new MaskingConsoleReader(in, new PrintWriter(System.out));
		ArgumentCompletor completor = new ArgumentCompletor(new Completor[] { new CommandCompletor(this.engine), new VfsFileNameCompletor(this.engine, true)}, new CustomArgumentDelimiter());
		completor.setStrict(false);
		this.consoleReader.addCompletor(completor);
		this.reader = new InputStreamReader(new ConsoleReaderInputStream(consoleReader));
		customizeEngine(engine);
		loadRc();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			if (args.length==0) {
				Shell shell = new Shell(System.in);
				shell.go();
			}
			else {
				String path = args[0];
				File file = new File(path);
				if (file.exists()) {
					InputStream in = null;
					try {
						in = new FileInputStream(file);
						Shell shell = new Shell(in);
						shell.go();
					}
					finally {
						try {
							if (in!=null) in.close();
						}
						catch (IOException e){}
					}
				}
			}
		}
		catch (Exception e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	    
		System.exit(0);
	}

	public void print(Object o) {
		try {
			consoleReader.printString(o==null?"null":o.toString());
			consoleReader.flushConsole();
			if (o!=null && o.toString().equals(maskTrigger)) {
				consoleReader.setMaskNext(true);
			}
		}
		catch (IOException e) {
			//ignore
		}		
	}

	public void println(Object o) {
		if (consoleReader==null) {
			System.out.println("console reader null while printing " + o);
		}
		try {
			consoleReader.printString(o==null?"null":o.toString());
			consoleReader.printNewline();
			consoleReader.flushConsole();
		}
		catch (IOException e) {
			//ignore
		}
	}
  
    
}
