package org.vfsutils.shell.sshd;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.factory.FileSystemManagerFactory;

public class VfsPasswordAuthenticator implements PasswordAuthenticator {
	
	protected static final Logger log = LoggerFactory.getLogger(VfsPasswordAuthenticator.class);

	private String rootPath;
	private boolean virtual;
	private String domain;
	
	private FileSystemManagerFactory factory;
	
	public VfsPasswordAuthenticator(FileSystemManagerFactory factory, String rootPath, boolean virtual) {
		this.factory = factory;
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

			FileSystemManager fileSystemManager = factory.getManager();
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
