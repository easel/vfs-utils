package org.vfsutils.ftpserver.usermanager;

import org.apache.commons.vfs.FileObject;
import org.apache.ftpserver.usermanager.impl.BaseUser;

/**
 * User that encapsulates a VFS File object representing its home dir.
 * Note that due to layering and virtualness the name of this home dir
 * does not necessarily have to match the name of HomeDirectory.
 * @author kleij - at - users.sourceforge.net
 *
 */
public class VfsUser extends BaseUser {
	
	private static final long serialVersionUID = -799346491877716615L;
	
	private FileObject vfsHomeDir = null;

	public FileObject getVfsHomeDir() {
		return this.vfsHomeDir;
	}

	public void getVfsHomeDir(FileObject vfsHomeDir) {
		this.vfsHomeDir = vfsHomeDir;
	}
	
	

}
