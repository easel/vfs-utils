package org.vfsutils.shell.commands;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Close extends AbstractOpenClose {

	public Close() {
		super("close", new CommandInfo("Close the connection", "[-a|<uri>|~<idx>]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		if (args.hasFlag("a")) {
			closeAll(engine);
		} else if (args.size() == 0) {
			close(engine);
		} else {
			String path = args.getArgument(0);
			if (path.startsWith("~") && path.length()>1){
				String subs = path.substring(1);
				//try to convert to int
				int i = Integer.parseInt(subs);
				close(i, engine);
			}
			else {
				close(path, engine);
			}
		}
	}

	protected void close(String path, Engine engine) throws FileSystemException {
		FileObject file = engine.pathToExistingFile(path);
		close(file, engine);
	}

	protected void close(Engine engine) throws FileSystemException {
		close(engine.getCwd(), engine);
	}

	protected void close(int i, Engine engine) throws CommandException, FileSystemException {
		List openFs = getOpenFs(engine);
		if (i<1 || i>openFs.size()) {
			throw new CommandException("Invalid index given: " + i);
		}
		FileObject file = (FileObject)openFs.get(i-1);
		close(file, engine);
	}
	
	protected void close(FileObject file, Engine engine)
			throws FileSystemException {
		List openFs = getOpenFs(engine);
		FileObject root = file.getFileSystem().getRoot();
		if (openFs.contains(root)) {
			// check for layered fs
			FileObject parentFs = engine.getCwd().getFileSystem()
					.getParentLayer();
			
			// get index of current fs (needed for cd afterwards)
			int index = openFs.indexOf(root);

			// close the filesystem
			FileSystem fs = file.getFileSystem();
			engine.getMgr().closeFileSystem(fs);
			// remove from list
			openFs.remove(index);
			engine.println("Closed " + engine.toString(root));

			if (parentFs != null) {
				// make sure parent is a folder
				if (parentFs.getType().equals(FileType.FILE)) {
					parentFs = parentFs.getParent();
				}
			} else if (index > 0) {
				// go the fs just above in the list
				parentFs = (FileObject) openFs.get(index - 1);
			} else {
				//empty list
				parentFs = engine.getMgr().getBaseFile();
			}
			engine.getContext().setCwd(parentFs);
			engine.println("Current folder is " + engine.toString(parentFs));
		}
	}

	protected void closeAll(Engine engine) throws FileSystemException {
		List openFs = getOpenFs(engine);

		// start from last one
		for (int i = openFs.size() - 1; i >= 0; i--) {
			// get file from name
			FileObject root = (FileObject) openFs.get(i);
			
			// close the filesystem
			FileSystem fs = root.getFileSystem();
			engine.getMgr().closeFileSystem(fs);
			// remove from list
			openFs.remove(i);
			engine.println("Closed " + engine.toString(root));
		}
	}

}
