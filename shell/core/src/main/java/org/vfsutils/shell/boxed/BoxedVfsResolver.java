package org.vfsutils.shell.boxed;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.xml.VfsResolver;

public class BoxedVfsResolver extends VfsResolver {

	public BoxedVfsResolver(FileObject root) {
		super(root);
	}

	/**
	 * Only allow relative names or names within the same file system as the root
	 */
	public FileObject resolveFile(String name) throws FileSystemException {

		FileObject root = getRoot();
		int index = name.indexOf("://");
		if (index != -1) {
			// absolute, only allow within same file system - when it starts
			// with the same root URI
			if (name.startsWith(root.getName().getRootURI())) {
				return root.getFileSystem().getFileSystemManager().resolveFile(
						root, name);
			} else {
				return null;
			}
		} else {
			// relative
			return root.resolveFile(name);
		}
	}

}
