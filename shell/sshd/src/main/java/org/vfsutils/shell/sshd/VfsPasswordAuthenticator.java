package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfsPasswordAuthenticator implements PasswordAuthenticator {
	
	protected static final Logger log = LoggerFactory.getLogger(VfsPasswordAuthenticator.class);

	private String rootPath = null;
	private boolean virtual = false;
	private String domain = null;
	
	FileSystemManager fileSystemManager;
	
	public VfsPasswordAuthenticator(FileSystemManager fileSystemManager, String rootPath, boolean virtual) {
		this.fileSystemManager = fileSystemManager;
		this.rootPath = rootPath;
		this.virtual = virtual;
	}

	public boolean authenticate(String username, String password, ServerSession session) {
		
		FileObject file = null;

		try {
			FileSystemOptions opts = new FileSystemOptions();

			if (username!=null || password!=null || domain!=null) {
			
				StaticUserAuthenticator auth = new StaticUserAuthenticator(
					domain, username, password);

			
				DefaultFileSystemConfigBuilder.getInstance()
					.setUserAuthenticator(opts, auth);
				
			}
			
			file = fileSystemManager.resolveFile(rootPath, opts);
			
			if (virtual) {
				file = fileSystemManager.createVirtualFileSystem(file);
			}
			
			session.setAttribute(VfsShellFactory.VFS_ROOT, file);
		
			return true;
		} catch (FileSystemException e) {
			log.debug("Error while authenticating user " + username, e);
			return false;
		}
			
		
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	

}
