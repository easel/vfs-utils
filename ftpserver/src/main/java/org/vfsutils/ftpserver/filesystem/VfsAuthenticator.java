package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.factory.FileSystemManagerFactory;
import org.vfsutils.session.UniqueFileSystemConfigBuilder;

public class VfsAuthenticator {
	
	private final Logger log = LoggerFactory.getLogger(VfsAuthenticator.class);
	
	private FileSystemManagerFactory factory = new FileSystemManagerFactory(); 
	
	private String vfsRoot = null;
	private String vfsType = "normal";
	private String vfsDomain = null;
	
	private boolean vfsShare = true;

	/**
	 * The VFS root is the connection string for the underlying
	 * file system. It can be empty in case the user's home directory  
	 * contains the full VFS URI
	 * @return the root path to the underlying file system
	 */
	public String getVfsRoot() {
		return vfsRoot;
	}

	/**
	 * The VFS root is the connection string for the underlying
	 * file system. It starts with the scheme (e.g. file:// or ftp://)
	 * and can contain specifics such as the host. If the path
	 * contains the user or password they will override the user-provided 
	 * credentials. If the root path is not set the user's home directory
	 * should contain the full VFS URI.
	 * @param vfsRoot
	 */
	public void setVfsRoot(String vfsRoot) {
		this.vfsRoot = vfsRoot;
	}
	
	/**
	 * There are three ways to use a VFS: virtual, layered and normal. 
	 * @see #setVfsType(String)
	 * @return 'virtual', 'layered' or 'normal'
	 */
	public String getVfsType() {
		return vfsType;
	}

	/** 
	 * There are three ways to use a VFS: virtual, layered and normal. Virtual means
	 * the root of the file system is put at the indicated folder; it is not possible
	 * to go up in the hierarchy. Layered means that the indicated file is in fact
	 * another virtual filesystem such as a zip file. Normal means that the indicated
	 * folder is a location in a filesystem; it is possible to go up in the hierarchy
	 * if the indicated path is not the root. The default is 'normal'.
	 * The vfs type is only taken into account when the vfs root is set. 
	 * @param vfsType 'virtual', 'layered' or 'normal'
	 */	 
	public void setVfsType(String vfsType) {
		this.vfsType = vfsType;
	}

	/**
	 * The VFS domain is the domain that should be added to the 
	 * authentication. It can be null.	 
	 * @return the domain to be used in authentication
	 */
	public String getVfsDomain() {
		return vfsDomain;
	}

	/**
	 * VFS allows the notion of domain while the FTP interface doesn't. 
	 * If left null no domain is used.
	 * @param vfsDomain the domain to be used in authentication	  
     */
	public void setVfsDomain(String vfsDomain) {
		this.vfsDomain = vfsDomain;
	}
	
	/**
	 * Returns the factory used to make FileSystemManager instances
	 * @return factory
	 */
	public FileSystemManagerFactory getFactory() {
		return factory;
	}

	/**
	 * Sets the factory used to make FileSystemManager instances
	 * @param factory
	 */
	public void setFactory(FileSystemManagerFactory factory) {
		this.factory = factory;
	}

	/**
	 * If true, then the filesystem is shared between sessions of the same user. If 
	 * false, each session will have its own filesystem.
	 * @return
	 */
	public boolean isVfsShare() {
		return vfsShare;
	}

	/**
	 * If true, then the file system is shared between sessions of the same user. If 
	 * false, each session will have its own file system.
	 * @param share, if true the file system will be shared amongst sessions
	 */
	public void setVfsShare(boolean share) {
		this.vfsShare = share;
	}

	/**
	 * Authenticates the user. If the vfs root is unset, the given home path 
	 * should contain a full VFS URI. If the vfs root is set, the root is 
	 * used as the root of the file system while the home path is the starting
	 * point of the user.
	 * @param user
	 * @param password
	 * @param homePath the path to the home folder of the user. If the vfs root
	 * is not set the home path should contain a full VFS URI.
	 * @return a FileObject representing the home folder of the user
	 * @throws FileSystemException
	 */
	public VfsInfo authenticate(String user, String password, String homePath) throws FileSystemException {
		
		StaticUserAuthenticator auth = new StaticUserAuthenticator(this.vfsDomain, user, password); 
        FileSystemOptions opts = new FileSystemOptions(); 
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        
        if (!this.vfsShare) {
        	UniqueFileSystemConfigBuilder.getInstance().setUniqueKey(opts);
        }

        FileSystemManager manager = getFactory().getManager();        
        
        FileObject rootDir;
        FileObject homeDir;
        
        if (this.vfsRoot==null) {
        	if (homePath!=null) {
                     homeDir = manager.resolveFile(homePath, opts);
                     if(vfsType.equals("chroot")) {
                        rootDir = manager.createVirtualFileSystem(homeDir);
                        homeDir = rootDir.resolveFile("/");
                     } else { // normal
                        rootDir = homeDir;
                     }
        	}
        	else {
        		throw new FileSystemException("homePath can not be null when there is no vfs root configured");
        	}
        } else {
        	FileObject virtualRootDir = manager.resolveFile(this.vfsRoot, opts);
        	
        	if (vfsType.equals("virtual")) {				
				rootDir = manager.createVirtualFileSystem(virtualRootDir);
			}
			else if (vfsType.equals("layered")){
				rootDir = manager.createFileSystem(virtualRootDir);
			}
			else {
				rootDir = virtualRootDir;
			}
			
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
        }		
    
        log.info("Authenticated user " + user + " based on filesystem " + rootDir.getFileSystem().toString());
        // if the file system is not shared it should be closed
        // TODO: what about virtual && layered fs?
        boolean shouldClose = ! (factory.isShare() && this.isVfsShare());
        VfsInfo info = new VfsInfo(rootDir, homeDir, shouldClose);
        return info;
	}

}
