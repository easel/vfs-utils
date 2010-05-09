package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;

public class VfsInfo {
	
	private boolean shared;
	private FileSystemManager manager;
	private FileObject rootDir;
	private FileObject homeDir;
	
	
	public VfsInfo(FileSystemManager manager, boolean shared, FileObject rootDir, FileObject homeDir) {
		this.manager = manager;
		this.shared = shared;
		this.rootDir = rootDir;
		this.homeDir = homeDir;
	}


	public boolean isShared() {
		return shared;
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
