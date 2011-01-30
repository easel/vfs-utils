package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileObject;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.Session.AttributeKey;
import org.apache.sshd.server.Command;


public class VfsShellFactory implements Factory<Command> {
	
	/**
	 * The root of the file system used by the shell
	 */
	public static final AttributeKey<FileObject> VFS_ROOT = new AttributeKey<FileObject>();
	/**
	 * The path within the root, it can contain $USER which will be resolved to the user name
	 */
	protected static final AttributeKey<String> VFS_PATH = new AttributeKey<String>();


	public Command create() {
		return new VfsShell();
	}	

}
