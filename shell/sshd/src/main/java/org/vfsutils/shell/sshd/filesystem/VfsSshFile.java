package org.vfsutils.shell.sshd.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.apache.sshd.server.SshFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.content.PseudoRandomAccessContent;
import org.vfsutils.content.PseudoWriteableRandomAccessContent;
import org.vfsutils.content.RandomAccessContentOutputStream;

public class VfsSshFile implements SshFile {
	
	private final Logger LOG = LoggerFactory.getLogger(VfsSshFile.class);
	
	private FileObject vfsFile;
	private VfsFileSystemView view;
	
	public VfsSshFile(FileObject vfsFile, VfsFileSystemView view) {
		this.vfsFile = vfsFile;
		this.view = view;
	}
	
	/**
	 * If the file exists, false is returned. Else if the name ends with a '/' 
	 * a folder is created, otherwise a file is created.
	 */
	public boolean create() throws IOException {
		
		if (vfsFile.exists()) {
			return false;
		} else if (vfsFile.getName().getType().hasChildren()){
			vfsFile.createFolder();
			return true;
		} else {
			vfsFile.createFile();
			return true;
		}
		
	}



	public InputStream createInputStream(long offset) throws IOException {
	
		RandomAccessMode ram = RandomAccessMode.READWRITE;
		RandomAccessContent rac;
		if (vfsFile.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_WRITE)) {
			rac = vfsFile.getContent().getRandomAccessContent(ram);
		}
		else {
			rac = new PseudoWriteableRandomAccessContent(vfsFile, ram);
		}
		rac.seek(offset);
		return rac.getInputStream();
	    
	}

	public OutputStream createOutputStream(long offset) throws IOException {
		RandomAccessMode ram = RandomAccessMode.READ;
		RandomAccessContent rac;
		if (this.vfsFile.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_READ)) {
			rac = vfsFile.getContent().getRandomAccessContent(ram);
		}
		else {
			rac = new PseudoRandomAccessContent(this.vfsFile, ram);
		}
		rac.seek(offset);
		return new RandomAccessContentOutputStream(rac);
	}

	public boolean delete() {
		try {
			return this.vfsFile.delete();
		} catch (FileSystemException e) {
			LOG.warn("Exception while deleting file " + this, e);
			return false;
		}
	}

	public boolean doesExist() {
		try {
			return vfsFile.exists();
		} catch (FileSystemException e) {
			LOG.warn("Error while checking existence of file " + this, e);
			//assume it exists?
			return true;
		}
	}

	public String getAbsolutePath() {
		return this.vfsFile.getName().getPath();		
	}

	public long getLastModified() {
		try {
	    	long modifiedTime = (this.vfsFile.exists()? this.vfsFile.getContent().getLastModifiedTime() : 0);
			return modifiedTime;
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting last modified time of file " + this, e);
			return 0;
		}
	}

	public String getName() {
		return this.vfsFile.getName().getBaseName();
	}

	public SshFile getParentFile() {
		try {
			FileObject parent = this.vfsFile.getParent();
			if (parent==null) {
				return this;
			}
			else {
				return new VfsSshFile(parent, this.view);
			}
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting parent file of " + this, e);
			return null;
		}
	}

	public long getSize() {
		try {
			long length =  (this.vfsFile.getType().hasContent() ? this.vfsFile.getContent().getSize() : 0);
			return length;
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting size of file " + this, e);
			return 0;
		}
	}

	public void handleClose() throws IOException {
		this.vfsFile.close();
	}

	public boolean isDirectory() {
		try {
			FileType fileType;		
			if (this.vfsFile.exists()) {
				fileType = this.vfsFile.getType();
			} else {
				fileType = this.vfsFile.getName().getType();
			}
			return fileType.hasChildren();
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting type of file " + this, e);
			return false;
		}
	}

	public boolean isFile() {
		try {
			FileType fileType;		
			if (this.vfsFile.exists()) {
				fileType = this.vfsFile.getType();
			} else {
				fileType = this.vfsFile.getName().getType();
			}
			return fileType.hasContent();
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting type of file " + this, e);
			return false;
		}
	}

	public boolean isReadable() {
		try {
			return this.vfsFile.isReadable();
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting read access on " + this, e);
			return false;
		}
	}

	public boolean isExecutable() {
		// Default directories to being executable
        // as on unix systems to allow listing their contents.
		return this.isDirectory();
	}

	public boolean isRemovable() {
		return isWritable();
	}

	public boolean isWritable() {
		try {
			return this.vfsFile.isWriteable();
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting write access on " + this, e);
			return false;
		}
	}

	public List<SshFile> listSshFiles() {
		try {
			FileObject[] children = this.vfsFile.getChildren();
			ArrayList<SshFile> files = new ArrayList<SshFile>(children.length);
			for (FileObject child : children) {
				files.add(new VfsSshFile(child, this.view));
			}
			return files;
		} catch (FileSystemException e) {
			return new ArrayList<SshFile>(0);
		}
	}

	public boolean mkdir() {
		try {
			this.vfsFile.createFolder();
			return(this.vfsFile.exists()); 
		} catch (FileSystemException e) {
			LOG.warn("Exception while making directory " + this, e);
			return false;
		}

	}

	public boolean move(SshFile destination) {
		try {
			this.vfsFile.moveTo(((VfsSshFile)destination).getVfsFile());
			return true;
		} catch (FileSystemException e) {
			LOG.warn("Exception while getting read access on " + this, e);
			return false;
		}

	}

	public boolean setLastModified(long time) {
		try {
			this.vfsFile.getContent().setLastModifiedTime(time);
			return true;
		} catch (FileSystemException e) {
			LOG.debug("Could not set last modified of " + this + " to " + time, e);
			return false;
		}
	}

	public void truncate() throws IOException {
		this.vfsFile.getContent().getInputStream().close();
	}
	
	public String toString() {
		return (this.vfsFile==null?"nullfile":this.vfsFile.getName().getFriendlyURI());
	}
	
	public FileObject getVfsFile() {
		return this.vfsFile;
	}

}
