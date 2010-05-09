package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileObject;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.Session.AttributeKey;
import org.apache.sshd.server.Command;
import org.vfsutils.factory.FileSystemManagerFactory;


public class VfsShellFactory implements Factory<Command> {
	
	/**
	 * The root of the file system used by the shell
	 */
	protected static final AttributeKey<FileObject> VFS_ROOT = new AttributeKey<FileObject>();
	/**
	 * The path within the root, it can contain $USER which will be resolved to the user name
	 */
	protected static final AttributeKey<String> VFS_PATH = new AttributeKey<String>();
	
	private FileSystemManagerFactory factory;
	private String path;
	
	public VfsShellFactory(FileSystemManagerFactory factory) {
		this.factory = factory;
		this.path = null;
	}

	public VfsShellFactory(FileSystemManagerFactory factory, String path) {
		this.factory = factory;
		this.path = path;
	}

	public Command create() {
		return new VfsShell(factory, path);
	}	

}
