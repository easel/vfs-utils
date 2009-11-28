package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.Session.AttributeKey;
import org.apache.sshd.server.Command;


public class VfsShellFactory implements Factory<Command> {
	
	/**
	 * The root of the file system used by the shell
	 */
	protected static final AttributeKey<FileObject> VFS_ROOT = new AttributeKey<FileObject>();
	/**
	 * The path within the root, it can contain $USER which will be resolved to the user name
	 */
	protected static final AttributeKey<String> VFS_PATH = new AttributeKey<String>();
	
	private FileSystemManager fsManager;
	private String path;
	
	public VfsShellFactory(FileSystemManager fsManager) {
		this.fsManager = fsManager;
		this.path = null;
	}

	public VfsShellFactory(FileSystemManager fsManager, String path) {
		this.fsManager = fsManager;
		this.path = path;
	}

	public Command create() {
		return new VfsShell(fsManager, path);
	}
	
	
	
	

}
