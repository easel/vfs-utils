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
		//avoid c:/, ftp:// etc.
		if (path.indexOf(":")!=-1) {
			//check the path more profoundly
			if (path.startsWith(getCwd().getName().getRootURI())) {
				return super.pathToFile(path);
			} else {
				throw new FileSystemException("Only local paths are allowed");
			}			
		}
		else {
			return super.pathToFile(path);
		}
	}

	public FileObject[] pathToFiles(String pathPattern, boolean depthFirst)
			throws FileSystemException, IllegalArgumentException {
		//avoid c:/, ftp:// etc
		if (pathPattern.indexOf(":")!=-1) {
			if (pathPattern.startsWith(getCwd().getName().getRootURI())) {
				return super.pathToFiles(pathPattern, depthFirst);
			} else {
				throw new FileSystemException("Only local paths are allowed");
			}	
		}
		else {
			return super.pathToFiles(pathPattern, depthFirst);
		}
	}

	public String toString(FileObject file) {
		return this.toString(file.getName());
	}
	
	public String toString(FileName filename) {
		try {
			//only show paths starting from the root of the fs
			return filename.getPathDecoded();
		} catch (FileSystemException e) {
			return "n/a";
		}
	}	

	public void close() {
    	
    	FileSystem fs = getCwd().getFileSystem();
    	
    	while (fs!=null) {
    		try {
	    		this.mgr.closeFileSystem(fs);
	    		FileObject parent = fs.getParentLayer();
	    		if (parent==null) {
	    			fs = null;
	    		} else {
	    			fs = parent.getFileSystem();
	    		}
	    	}
    		catch (FileSystemException e) {
    			error("Error closing engine: " + e.getMessage());
    		}
    		
    	}
    }

	
}
