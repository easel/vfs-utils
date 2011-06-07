package org.vfsutils.ftpserver.usermanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.ftpserver.filesystem.VfsAuthenticator;
import org.vfsutils.ftpserver.filesystem.VfsInfo;

/**
 * Apache VFS implementation of the UserManager. There is no 
 * information available about a user before authentication.
 * The provider of the underlying file system and the type 
 * (normal, virtual or layered) can be configured as well
 * as the path to the home directory of the user. For the 
 * authentication a domain can be specified in the configuration
 * since the VFS framework allows it, but the information is 
 * not provided by the FTP interface.
 * 
 * @author kleij - at - users.sourceforge.net
 *
 */
public class VfsUserManager extends UserProps implements UserManager {
	
	private final Logger log = LoggerFactory.getLogger(VfsUserManager.class);
	
	//configurable instance variables
	protected String vfsHomePath = "";
	protected VfsAuthenticator authenticator = new VfsAuthenticator();
	protected String adminName = "admin";
	
	protected String anonymousName ="anonymous";
	protected String anonymousPwd = "anon@localhost";
	
	protected UserProps admin = new UserProps();
	
	/**
	 * The home path is the path to the home directory of the user.
	 * It can be relative to the root or absolute. Note that path
	 * can contain the ${user} variable which will be resolved 
	 * dynamically. The home path defaults to ${user}.
	 * @return The home path
	 */
	public String getVfsHomePath() {
		return vfsHomePath;
	}

	/**
	 * Sets the home path of the user. It can be relative to the 
	 * root or absolute and can contain the ${user} variable.
	 * @param vfsHomePath
	 */
	public void setVfsHomePath(String vfsHomePath) {
		this.vfsHomePath = vfsHomePath;
	}
	
	public VfsAuthenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(VfsAuthenticator authenticator) {
		this.authenticator = authenticator;
	}
	
	public String getAdminName() {
		return this.adminName;
	}
	
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	
	public String getAnonymousName() {
		return this.anonymousName;
	}
	
	public void setAnonymousName(String anonymousName) {
		this.anonymousName = anonymousName;
	}
	
	public String getAnonymousPwd() {
		return this.anonymousPwd;
	}
	
	public void setAnonymousPwd(String anonymousPwd) {
		this.anonymousPwd = anonymousPwd;
	}
	
	public UserProps getAdmin() {
		return admin;
	}

	public void setAdmin(UserProps admin) {
		this.admin = admin;
	}

	//interface implementation
	public User authenticate(Authentication authentication)
			throws AuthenticationFailedException {
		
		if(authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            
            String user = upauth.getUsername(); 
            String password = upauth.getPassword(); 
        
            if(user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
            if(password == null) {
                password = "";
            }
                
            if (this.authenticator==null) {
            	throw new AuthenticationFailedException("No authenticator set");
            }
            
            try {
            	String adaptedHomePath = vfsHomePath.replaceAll("\\$\\{user\\}", String.valueOf(user));
            	VfsInfo info = this.authenticator.authenticate(user, password, adaptedHomePath);
            	
            	VfsUser userObject = (VfsUser) getUserByName(user);
            	userObject.setHomeDirectory(info.getHomeDir().getName().getURI());
                userObject.setVfsInfo(info);                
                
                return userObject;
            	
            }
            catch (FileSystemException e) {
            	log.error("Error in authentication", e);
            	throw new AuthenticationFailedException("Authentication failed: " + e.getMessage(), e);
            }
                        
        } else if(authentication instanceof AnonymousAuthentication) {
            if(doesExist("anonymous")) {
            	String user = this.anonymousName;
            	String password = this.anonymousPwd;
            	try {
	            	String adaptedHomePath = vfsHomePath.replaceAll("\\$\\{user\\}", String.valueOf(user));
	            	VfsInfo info = this.authenticator.authenticate(user, password, adaptedHomePath);
	            	
	            	VfsUser userObject = (VfsUser) getUserByName(user);
	            	userObject.setHomeDirectory(info.getHomeDir().getName().getURI());
	                userObject.setVfsInfo(info);                
	                
	                return userObject;
            	}
                catch (FileSystemException e) {
                	log.error("Error in authentication", e);
                	throw new AuthenticationFailedException("Authentication failed: " + e.getMessage(), e);
                }
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }
        } else {
            throw new IllegalArgumentException("Authentication not supported by this user manager");
        }
	}

	public void delete(String login) throws FtpException {
		// TODO Auto-generated method stub

	}

	/**
	 * There is no way of knowing if a user exists until authentication is done. Therefore this method will
	 * always return true.
	 */
	public boolean doesExist(String login) {
		return true;
	}

	public String[] getAllUserNames() throws FtpException {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByName(String login) {
		VfsUser user = new VfsUser();
        user.setName(login);
        user.setEnabled(true);
        user.setHomeDirectory("/");
        
        List<Authority> authorities = new ArrayList<Authority>();
        
        //choose appropriate user properties (normal or admin)
        UserProps userProps;
        if (this.isAdmin(login)) {
        	userProps = this.admin;
        	user.setAdmin(true);
        }
        else {
        	userProps = this;
        }
        
        if (userProps.isWritePermission()) {
        	authorities.add(new WritePermission());
        }
        
        authorities.add(new ConcurrentLoginPermission(userProps.maxLogin, userProps.maxLoginPerIp));
        
        authorities.add(new TransferRatePermission(userProps.downloadRate, userProps.uploadRate));
        
        user.setAuthorities(authorities);
        
        user.setMaxIdleTime(userProps.maxIdleTime);
            
        return user;

	}

	public boolean isAdmin(String login) {
		if (this.adminName==null) {
			return false;
		}
		else {
			return this.adminName.equals(login);
		}
	}

	public void save(User user) throws FtpException {
		// TODO Auto-generated method stub

	}

}
