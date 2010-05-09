package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			org.apache.commons.vfs.FileObject changedDir = this.workingDir.getVfsFile().resolveFile(dir);
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
		FileSystemManager fsm = fs.getFileSystemManager();
		
		boolean shared = this.vfsInfo.isShared();
		
		log.info("Dispose file system view using " + (shared?"shared":"dedicated") + " file system manager");
		
		// releasing handles
		this.workingDir = null;
		this.homeDir = null;
		this.vfsInfo = null;
		this.user = null;
		
		if (shared && fsm instanceof DefaultFileSystemManager) {
			// this is a bit too wide because it will iterate over all providers and all filesystems (per user)
			((DefaultFileSystemManager)fsm).freeUnusedResources();
		}
		else if (!shared) {
			// too brutal to use when the fsm is shared
			fsm.closeFileSystem(fs);
		}
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
