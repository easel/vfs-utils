package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.ftpserver.usermanager.VfsUser;

/**
 * FileSystemView on a VFS filesystem. The input for this class is a Apache Commons VFS FileObject
 * representing the homedir of the user. 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class VfsFileSystemView implements FileSystemView {

	private final Logger log = LoggerFactory.getLogger(VfsFileSystemView.class);
	
	private VfsFtpFile workingDir;
	private VfsFtpFile homeDir;
	private User user;
	private VfsInfo vfsInfo;
	
	/**
	 * Creates a new FileSystemView based on the passed Apache Commons VFS FileObject.
	 * This homeFilObject should exist and be folder-like.
	 * @param homeFileObject the FileObject representing the home directory of the user
	 */
	public VfsFileSystemView(VfsInfo vfsInfo, User user) {
		this.vfsInfo = vfsInfo;
		this.user = user;
		
		this.homeDir = new VfsFtpFile(vfsInfo.getHomeDir(), user);
		this.workingDir = homeDir;
	}
	
	public boolean changeWorkingDirectory(String dir) throws FtpException {
		boolean result = false;
		
		try {
			org.apache.commons.vfs2.FileObject changedDir = this.workingDir.getVfsFile().resolveFile(dir);
			if (changedDir.getType().hasChildren()) {
				this.workingDir = new VfsFtpFile(changedDir, user);
				result = true;
			}
			else {
				result = false;
			}
		} catch (FileSystemException e) {
			throw new FtpException("Could not change to directory " + dir, e);
		}
		
		return result;
	}

	public void dispose() {
		FileSystem fs = this.workingDir.getVfsFile().getFileSystem();
		String userName = this.user.getName();
		boolean isAdmin = false;
		
		if (user instanceof VfsUser && ((VfsUser)user).isAdmin()) {
			isAdmin = true;
		}
		
		boolean shouldClose = vfsInfo.isShouldClose();
		String fsId = fs.toString();
		
		// release handles
		this.workingDir = null;
		this.homeDir = null;
		this.vfsInfo = null;
		this.user = null;
		
		if (shouldClose) {
			log.info("Closing fs");
			fs.getFileSystemManager().closeFileSystem(fs);
		}
		else {
			// close the file system if there are no handles anymore (like
			// is done in AbstractFileProvider.freeUnusedResources
			if (fs instanceof AbstractFileSystem) {
				AbstractFileSystem afs = (AbstractFileSystem) fs;
				if (isAdmin && fs.getFileSystemManager() instanceof DefaultFileSystemManager) {
					log.info("Forcing gc and finalization");
					// force gc and finalization to free references to filesystems
					System.gc();
					System.runFinalization();
				} 
				// kick all filesystems
				((DefaultFileSystemManager) fs.getFileSystemManager()).freeUnusedResources();
			}
		}		
		log.info("Disposed file system view of user " + userName + " based on filesystem " + fsId);
		
		
	}

	public FtpFile getWorkingDirectory() throws FtpException {
		return this.workingDir;
	}

	public FtpFile getFile(String file) throws FtpException {
		try {
			return new VfsFtpFile(this.workingDir.getVfsFile().resolveFile(file), user);
		} catch (FileSystemException e) {
			throw new FtpException("Could not get file " + file, e);
		}
	}

	public FtpFile getHomeDirectory() throws FtpException {
		return this.homeDir;
	}

	public boolean isRandomAccessible() throws FtpException {
		FileSystem fs = this.homeDir.getVfsFile().getFileSystem();
		boolean randomAccessible = fs.hasCapability(Capability.RANDOM_ACCESS_READ);
		if (randomAccessible && fs.hasCapability(Capability.WRITE_CONTENT)) {
			randomAccessible = fs.hasCapability(Capability.RANDOM_ACCESS_WRITE);
		}
		return randomAccessible;
	}

}
