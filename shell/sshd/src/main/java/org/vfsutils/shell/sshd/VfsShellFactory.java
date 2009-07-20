package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.sshd.common.session.AttributeKey;
import org.apache.sshd.server.ShellFactory;

public class VfsShellFactory implements ShellFactory {
	
	/**
	 * The root of the file system used by the shell
	 */
	protected static final AttributeKey<FileObject> VFS_ROOT = new AttributeKey<FileObject>();
	/**
	 * The path within the root, it can contain $USER which will be resolved to the user name
	 */
	protected static final AttributeKey<String> VFS_PATH = new AttributeKey<String>();
	
	/**
	 * Since there isn't any communication between VfsPasswordAuthentication and the VfsShell yet
	 * use a ThreadLocal to pass values
	 */
	protected static ThreadLocal<FileObject> vfsRoot = new ThreadLocal<FileObject>();
	protected static ThreadLocal<String> vfsPath = new ThreadLocal<String>();
	
	
	private FileSystemManager fsManager;
	private String rootPath = null;
	
	public VfsShellFactory(FileSystemManager fsManager) {
		this.fsManager = fsManager;
	}

	public VfsShellFactory(FileSystemManager fsManager, String rootPath) {
		this.fsManager = fsManager;
		this.rootPath = rootPath;
	}
	
	public Shell createShell() {
		return new VfsShell(fsManager, rootPath);
	}
	
	

}
