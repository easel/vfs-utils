package org.vfsutils.shell.sshd.filesystem;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.vfsutils.factory.FileSystemManagerFactory;
import org.vfsutils.shell.sshd.VfsShellFactory;

public class VfsFileSystemFactory implements FileSystemFactory {

	private String rootPath;
	private String homePath;
	private String vfsType="standard";

	private boolean cacheRoot = false;
	private FileObject cachedRoot = null;
	
	private FileSystemManagerFactory fsManagerFactory;
	
	public VfsFileSystemFactory(FileSystemManagerFactory factory, String rootPath, String vfsType, String homePath) throws FileSystemException {
		this.fsManagerFactory = factory;
		this.rootPath = rootPath;
		this.vfsType = vfsType;
		this.homePath = homePath;
	}
	
	public FileSystemView createFileSystemView(Session session) throws IOException {

		FileObject storedRoot = session.getAttribute(VfsShellFactory.VFS_ROOT);
		
		FileObject rootDir;
		if (storedRoot != null) {
			rootDir = storedRoot;
		}
		else {
			rootDir = this.getRoot();				
		}
		
		FileObject homeDir;
		if (homePath==null) {
    		homeDir = rootDir;
    	}
    	else {
    		homeDir = rootDir.resolveFile(homePath);
    	
    		if (!homeDir.exists()) {
    			//TODO log warning?
    			homeDir = rootDir;
    		}
    	}
		
		return new VfsFileSystemView(homeDir);
	}

	protected FileObject getRoot() throws FileSystemException {
		if (this.cacheRoot) {
			if (this.cachedRoot==null) {
				this.cachedRoot = createRoot();
			}
			return this.cachedRoot;
		} else {
			return createRoot();
		}
	}
	
	protected FileObject createRoot() throws FileSystemException {
		
		FileSystemManager manager = this.fsManagerFactory.getManager();
		
		FileObject virtualRootDir = manager.resolveFile(this.rootPath);
		FileObject rootDir;
		
    	
    	if (vfsType.equals("virtual")) {				
			rootDir = manager.createVirtualFileSystem(virtualRootDir);
		}
		else if (vfsType.equals("layered")){
			rootDir = manager.createFileSystem(virtualRootDir);
		}
		else {
			rootDir = virtualRootDir;
		}
		
    	return rootDir;
	}
}
