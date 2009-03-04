package org.vfsutils.shell.boxed;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandRegistry;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.commands.Open;

import bsh.ConsoleInterface;

public class BoxedEngine extends Engine {

	public BoxedEngine(ConsoleInterface console, CommandRegistry reg,
			FileSystemManager mgr) throws FileSystemException {
		super(console, reg, mgr);
	}
		
	
	public void setStartDir(FileObject startDir) throws FileSystemException {
		this.context.setCwd(startDir);
	}
	
	public void setStartDir(String path, boolean askUsername, boolean askPassword, boolean askDomain, boolean virtual) throws FileSystemException {
		try {
			Open openCmd = new Open();
			openCmd.open(path, askUsername, askPassword, askDomain, virtual, this);
		}
		catch (CommandException e) {
			throw new FileSystemException(e);
		}
	}
	
	public void setStartDir(String path, String username, String password, String domain, boolean virtual) throws FileSystemException {
		try {
			Open openCmd = new Open();
			openCmd.open(path, username, password, domain, virtual, this);
		}
		catch (CommandException e) {
			throw new FileSystemException(e);
		}
	}

	public FileObject pathToFile(String path) throws FileSystemException {
		if (path.indexOf("://")!=-1) {
			throw new FileSystemException("Only relative paths are allowed");
		}
		else {
			return super.pathToFile(path);
		}
	}

	public FileObject[] pathToFiles(String pathPattern)
			throws FileSystemException, IllegalArgumentException {
		if (pathPattern.indexOf("://")!=-1) {
			throw new FileSystemException("Only relative paths are allowed");
		}
		else {
			return super.pathToFiles(pathPattern);
		}
	}

	public String toString(FileName filename) {
		try {
			return filename.getPathDecoded();
		} catch (FileSystemException e) {
			return "n/a";
		}
	}	

	public void close() throws FileSystemException {
    	
    	FileSystem fs = getCwd().getFileSystem();
    	
    	while (fs!=null) {
    		this.mgr.closeFileSystem(fs);
    		FileObject parent = fs.getParentLayer();
    		if (parent==null) {
    			fs = null;
    		} else {
    			fs = parent.getFileSystem();
    		}
    	}
    }

	
}
