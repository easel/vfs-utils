package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

/**
 * FileSystemView on a VFS filesystem. The input for this class is a Apache Commons VFS FileObject
 * representing the homedir of the user. 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class VfsFileSystemView implements FileSystemView {
	
	private VfsFtpFile workingDir;
	private VfsFtpFile homeDir;
	private User user;
	
	/**
	 * Creates a new FileSystemView based on the passed Apache Commons VFS FileObject.
	 * This homeFilObject should exist and be folder-like.
	 * @param homeFileObject the FileObject representing the home directory of the user
	 */
	public VfsFileSystemView(org.apache.commons.vfs.FileObject homeFileObject, User user) {
		this.homeDir = new VfsFtpFile(homeFileObject, user);
		this.workingDir = homeDir;
		this.user = user;		
	}
	
	public boolean changeWorkingDirectory(String dir) throws FtpException {
		boolean result = false;
		
		try {
			org.apache.commons.vfs.FileObject changedDir = this.workingDir.getVfsFile().resolveFile(dir);
			if (changedDir.getType().equals(FileType.FOLDER) || changedDir.getType().equals(FileType.FILE_OR_FOLDER)) {
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
		fs.getFileSystemManager().closeFileSystem(fs);
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
