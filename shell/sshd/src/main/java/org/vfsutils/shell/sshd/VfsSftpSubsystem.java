package org.vfsutils.shell.sshd;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.factory.FileSystemManagerFactory;

/**
 * SFTP subsystem
 * Copy of org.apache.sshd.server.sftp.SftpSubsystem and adapted for VFS where needed.
 * @see org.apache.sshd.server.sftp.SftpSubsystem 
 * @author kleij - at - users.sourceforge.net
 */
public class VfsSftpSubsystem extends org.apache.sshd.server.sftp.SftpSubsystem implements Command, Runnable, SessionAware {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
    
	public static class Factory implements NamedFactory<Command> {
        
		private FileSystemManagerFactory factory;
		private String rootPath;
		private String path;
		
		public Factory(FileSystemManagerFactory factory, String rootPath, String path) {
			this.factory = factory;
			this.rootPath = rootPath;
			this.path = path;
		}
		
		public Command create() {		
            return new VfsSftpSubsystem(factory, this.rootPath, this.path);
        }
		
        public String getName() {
            return "sftp";
        }
    }

    // the factory and rootpath are set via the constructor
    private FileSystemManagerFactory factory;
    private String rootPath;
    
    // the root can be overridden via the session
	private FileObject root;
	private String path;

    public VfsSftpSubsystem(FileSystemManagerFactory factory, String rootPath, String homePath) {
    	super();
    	this.factory = factory;
    	this.rootPath = rootPath;
    }
    
    /**
     * Try to retrieve the root from the session in case it was set by the 
     * VFS password authenticator.
     */
	public void setSession(ServerSession session) {
		super.setSession(session);
		
		FileObject storedRoot = session.getAttribute(VfsShellFactory.VFS_ROOT);
		if (storedRoot != null) {
			this.root = storedRoot;
		}
		
		String storedPath = session.getAttribute(VfsShellFactory.VFS_PATH);
		if (storedPath != null) {
			this.path = storedPath;
		}
	}

	/**
	 * Starts the subsystem. If no root was injected through the session then the root
	 * will be resolved using the filesystem manager and root that were passed in the 
	 * constructor.
	 */
    public void start(Environment env) throws IOException {
        
        if (this.root==null){
        	if (this.factory==null || this.rootPath==null) {
        		throw new IOException("The root should be set via the session or via the constructor");
        	}
        	FileSystemManager fsManager = this.factory.getManager();
        	this.root = fsManager.resolveFile(this.rootPath);
        }
        
        super.start(env);
    }

    
}
