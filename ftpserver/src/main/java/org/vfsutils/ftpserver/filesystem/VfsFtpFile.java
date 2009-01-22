package org.vfsutils.ftpserver.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfsFtpFile implements FtpFile {
	
	private final Logger log = LoggerFactory.getLogger(VfsFtpFile.class);
	
	private org.apache.commons.vfs.FileObject vfsFile;
	
	private User user;

	public VfsFtpFile(org.apache.commons.vfs.FileObject object, User user) {
		this.vfsFile = object;
		this.user = user;
	}

	protected org.apache.commons.vfs.FileObject getVfsFile() {
		return this.vfsFile;
	}
	
	public InputStream createInputStream(long offset) throws IOException {
		if (offset>0) {
			throw new IOException("offset not supported");
		}
		return this.vfsFile.getContent().getInputStream();
	}

	public OutputStream createOutputStream(long offset) throws IOException {
		if (offset>0) {
			throw new IOException("offset not supported");
		}
		return this.vfsFile.getContent().getOutputStream();
	}

	public boolean delete() {
		boolean result = false;
		try {
			result = this.vfsFile.delete();
		}
		catch (FileSystemException e) {
			log.error("Error deleting file " + this.vfsFile.getName(), e);
			result = false;
		}
		return result;
	}

	public boolean doesExist() {
		boolean result = false;
		try {
			result = this.vfsFile.exists();
		} catch (FileSystemException e) {
			log.error("Error determining existence of file " + this.vfsFile.getName(), e);
			result = false;
		}
		return result;
	}

	public String getAbsolutePath() {
		return this.vfsFile.getName().getPath();
	}

	public String getGroupName() {
		return "group";
	}

	public long getLastModified() {
		long result = 0;
		
		try {
			result =  this.vfsFile.getContent().getLastModifiedTime();
		} catch (FileSystemException e) {
			log.error("Error getting last modified of " + this.vfsFile.getName(), e);
		}
		
		return result;
	}
	
	public boolean setLastModified(long time) {
		try {
			this.vfsFile.getContent().setLastModifiedTime(time);
			return true;
		} catch (FileSystemException e) {
			log.debug("Could not set last modified of " + this.vfsFile.getName() + " to " + time, e);
			return false;
		}
	}

	public int getLinkCount() {
		return 0;
	}

	public String getOwnerName() {
		return "owner";
	}

	public String getName() {
		return this.vfsFile.getName().getBaseName();
	}

	public long getSize() {
		long result = 0;
		try {
			result = this.vfsFile.getContent().getSize();
		} catch (FileSystemException e) {
			log.error("Error getting size of " + this.vfsFile.getName(), e);
		}
		return result;
	}

	public boolean isRemovable() {
		//writable is the closest we can get
		boolean result = false;
		try {
			result = this.vfsFile.isWriteable();
		} catch (FileSystemException e) {
			log.error("Error determining delete permission on " + this.vfsFile.getName(), e);
		}
		return result;
	}

	public boolean isReadable() {
		boolean result = false;
		
		try {
			result = this.vfsFile.isReadable();
		} catch (FileSystemException e) {
			log.error("Error determining read permission on " + this.vfsFile.getName(), e);
		}
		
		return result;
	}

	public boolean isWritable() {
		
		if (this.user.authorize(new WriteRequest(getAbsolutePath())) == null) {
            return false;
        }
		
		boolean result = false;
		
		try {
			result = this.vfsFile.isWriteable();
		} catch (FileSystemException e) {
			log.error("Error determining write permission on " + this.vfsFile.getName(), e);
		}
		
		return result;
	}

	public boolean isDirectory() {
		boolean result = false;
		
		try {
			result = this.vfsFile.getType().equals(FileType.FOLDER) || 
				this.vfsFile.getType().equals(FileType.FILE_OR_FOLDER);
		} catch (FileSystemException e) {
			log.error("Error determining directory type on " + this.vfsFile.getName(), e);
		}
		return result;
		
	}

	public boolean isFile() {
		boolean result = false;
		
		try {
			result = this.vfsFile.getType().equals(FileType.FILE) || 
				this.vfsFile.getType().equals(FileType.FILE_OR_FOLDER);
		} catch (FileSystemException e) {
			log.error("Error determining file type on " + this.vfsFile.getName(), e);
		}
		return result;
	}

	public boolean isHidden() {
		boolean result = false;
		try {
			result = this.vfsFile.isHidden();
		} catch (FileSystemException e) {
			log.error("Error determining if hidden on " + this.vfsFile.getName(), e);
		}
		return result;
	}

	public List<FtpFile> listFiles() {
		List<FtpFile> files = null;
		
		try {
			org.apache.commons.vfs.FileObject[] children = this.vfsFile.getChildren();
			files = new ArrayList<FtpFile>(children.length);
			for (int i=0; i<children.length;i++) {
				files.add(new VfsFtpFile(children[i], this.user));
			}
		} catch (FileSystemException e) {
			log.error("Error listing files of " + this.vfsFile.getName(), e);
		}
		
		return files;
	}

	public boolean mkdir() {
		boolean result = false;
		try {
			this.vfsFile.createFolder();
			result = true;
		} catch (FileSystemException e) {
			log.error("Error making dir " + this.vfsFile.getName(), e);
		}
		return result;
	}

	public boolean move(FtpFile destination) {
		boolean result = false;
		if (destination instanceof VfsFtpFile) {
			VfsFtpFile vfsDestination = (VfsFtpFile) destination;
			try {
				this.vfsFile.moveTo(vfsDestination.vfsFile);
				result = true;
			} catch (FileSystemException e) {
				log.error("Error moving " + this.vfsFile.getName() + " to " + destination.getAbsolutePath(), e);
			}
		}
		return result;
	}

}
