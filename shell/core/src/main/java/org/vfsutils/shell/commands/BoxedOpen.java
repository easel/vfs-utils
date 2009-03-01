package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class BoxedOpen extends Open {

	public BoxedOpen() {
		super();
		this.setDescription("Open a layered filesystem");
	}

	protected FileObject resolvePath(String path, boolean askUsername, boolean askPassword,
			boolean askDomain, boolean virtual, Engine engine)
			throws FileSystemException, CommandException {

		FileObject file;
		if (path.indexOf("://") == -1) {
			FileObject layeredFile = engine.pathToFile(path);

			if (virtual) {
				file = engine.getMgr().createVirtualFileSystem(layeredFile);
			} else {
				file = engine.getMgr().createFileSystem(layeredFile);
			}

		} else {
			throw new CommandException("Only relative paths are allowed");
		}

		return file;
	}

}
