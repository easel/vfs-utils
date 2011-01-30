package org.vfsutils.shell.sshd.filesystem;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.SshFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfsFileSystemView implements FileSystemView {
	
	private final Logger LOG = LoggerFactory.getLogger(VfsFileSystemView.class);
	
	private FileObject base;
	
	public VfsFileSystemView(FileObject base) {
		this.base = base;
	}

	public SshFile getFile(String file) {
		
		try {
			FileObject vfsFile = base.resolveFile(file);
			return new VfsSshFile(vfsFile, this);
		} catch (FileSystemException e) {
			LOG.warn("Could not get file " + file, e);
			return null;
		}
	}
	
	
	public SshFile getFile(SshFile baseDir, String file) {
		try {
			FileObject vfsBaseDir = ((VfsSshFile)baseDir).getVfsFile();
			FileObject vfsFile = vfsBaseDir.resolveFile(file);
			return new VfsSshFile(vfsFile, this);
		} catch (FileSystemException e) {
			LOG.warn("Could not get file " + file, e);
			return null;
		}
	}

	protected FileObject getBase() {
		return base;
	}

}
