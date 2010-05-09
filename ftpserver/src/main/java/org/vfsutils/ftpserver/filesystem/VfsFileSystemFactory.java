package org.vfsutils.ftpserver.filesystem;

import org.apache.commons.vfs.FileSystemException;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.vfsutils.ftpserver.usermanager.VfsUser;

/**
 * VFS based FileSystemManager that works with any user manager.
 * @author kleij - at - users.sourceforge.net
 *
 */
public class VfsFileSystemFactory implements FileSystemFactory {

	private VfsAuthenticator authenticator = new VfsAuthenticator();
	
	public VfsAuthenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(VfsAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	/**
	 * Creates a FileSystemView for the given user. It can be used with any user manager.
	 * If the user was created with the VfsUserManager the configuration used in the VfsUserManager is used. 
	 * Otherwise, the local authenticator is used. In this case the user's home directory should contain a 
	 * full VFS URI or the VFS root of the authenticator should be set to a valid VFS URI.
	 * @param user the user to create the view for
	 */
	public FileSystemView createFileSystemView(User user) throws FtpException {
				
		String homePath = user.getHomeDirectory();
		
		try {
			
			VfsInfo vfsInfo;
			
			if (user instanceof VfsUser) {
				//use the vfs file object from the user
				VfsUser vfsUser = (VfsUser) user;
				vfsInfo = vfsUser.getVfsInfo();
			}
			else {
				//do authentication
				vfsInfo = this.authenticator.authenticate(user.getName(), user.getPassword(), user.getHomeDirectory());
			}
			
			//test for proper configuration
			if (!vfsInfo.getHomeDir().exists()) {
				throw new FtpException("Home directory " + vfsInfo.getHomeDir() + " of user " + user.getName() + " does not exist");
			}
			
			if (!vfsInfo.getHomeDir().getType().hasChildren()) {
				throw new FtpException("Home directory " + vfsInfo.getHomeDir() + " of user " + user.getName() + " is not a folder");
			}
			
			return new VfsFileSystemView(vfsInfo, user);			
			
		} catch (FileSystemException e) {
			throw new FtpException("Error resolving home directory " + homePath + " of user " + user.getName(), e);
		}		
	}

}
