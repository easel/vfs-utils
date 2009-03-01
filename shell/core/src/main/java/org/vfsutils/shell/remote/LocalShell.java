package org.vfsutils.shell.remote;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Shell;

public class LocalShell extends Shell {
	
	public LocalShell(InputStream in) throws FileSystemException {
		
		this.engine = new org.vfsutils.shell.mina.LocalEngine(this);
		this.reader = new InputStreamReader(in);
	}
	
	public static void main(String[] args) {
		
		try {
			Shell shell = new LocalShell(System.in);
			shell.go();
		}
		catch (Exception e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	    
		System.exit(0);
	}

}
