package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;

public class VfsInfo {
	
	private FileSystemManager manager;
	private FileObject rootDir;
	private FileObject homeDir;
	
	
	public VfsInfo(FileSystemManager manager, FileObject rootDir, FileObject homeDir) {
		this.manager = manager;
		this.rootDir = rootDir;
		this.homeDir = homeDir;
	}


	public FileSystemManager getManager() {
		return manager;
	}


	public FileObject getRootDir() {
		return rootDir;
	}


	public FileObject getHomeDir() {
		return homeDir;
	}

	

}
