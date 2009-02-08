package org.vfsutils.shell.commands;

import java.util.List;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Close extends AbstractOpenClose {
	

	public Close() {
		super("close", new CommandInfo("Close the connection", "[-a|<uri>]"));
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		if (args.hasFlag("a")) {
			closeAll(engine);
		}
		else if (args.getArguments().size()==0) {
			close(engine);
		}
		else {
		String path = args.getArgument(0);
		close(path, engine);
	}
	}
	
	protected void close (String path, Engine engine) throws FileSystemException {
		FileObject file = engine.pathToFile(path);
		close(file, engine);
	}
	
	protected void close(Engine engine) throws FileSystemException {
		close(engine.getCwd(), engine);		
	}
	
	protected void close(FileObject file, Engine engine) throws FileSystemException {
		List openFs = getOpenFs(engine);
		FileName root = file.getName().getRoot();
		if (openFs.contains(root)) {
			//check for layered fs
			FileObject parentFs = engine.getCwd().getFileSystem().getParentLayer();
			//get index of current fs (needed for cd afterwards)
			int index = openFs.indexOf(root);
			
			//close the filesystem
			FileSystem fs = file.getFileSystem();
			engine.getMgr().closeFileSystem(fs);
			//remove from list
    		openFs.remove(index);
    		engine.println("Closed " + engine.toString(root));
    		
    		if (parentFs!=null) {
    			//make sure parent is a folder
    			if (parentFs.getType().equals(FileType.FILE)) {
    				parentFs = parentFs.getParent();
    			}
    		}
    		else if (index>0){
    			//go the fs just above in the list
    			FileName parentName = (FileName) openFs.get(index-1);
    			parentFs = engine.getMgr().resolveFile(parentName.getURI());
    		}
    		else {
    			parentFs = engine.getMgr().getBaseFile();
    		}
    		engine.getContext().setCwd(parentFs);
			engine.println("Current folder is " + engine.toString(parentFs));
		}
	}
	
	protected void closeAll(Engine engine) throws FileSystemException {
		List openFs = getOpenFs(engine);
		
		//start from last one
		for (int i=openFs.size()-1; i >=0; i--) {
			//get file from name
			FileName root = (FileName) openFs.get(i);			
			FileObject file = engine.getMgr().resolveFile(root.getURI());
			//close the filesystem
			FileSystem fs = file.getFileSystem();
			engine.getMgr().closeFileSystem(fs);
			//remove from list
    		openFs.remove(i);
    		engine.println("Closed " + engine.toString(root));
		}
	}

}
