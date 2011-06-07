package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs2.FileObject;

public class VfsInfo {
	
	private FileObject rootDir;
	private FileObject homeDir;
	private boolean shouldClose;
	
	
	public VfsInfo(FileObject rootDir, FileObject homeDir, boolean shouldClose) {
		this.rootDir = rootDir;
		this.homeDir = homeDir;
		this.shouldClose = shouldClose;
	}

	public FileObject getRootDir() {
		return rootDir;
	}

	public FileObject getHomeDir() {
		return homeDir;
	}

	public boolean isShouldClose() {
		return shouldClose;
	}
	
	

	

}
