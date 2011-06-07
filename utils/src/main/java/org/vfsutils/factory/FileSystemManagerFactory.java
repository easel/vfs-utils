package org.vfsutils.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;

/**
 * This is a configurable replacement of the default VFS class.
 * You can configure which class to instantiate and whether it
 * should be reused. Creating a new manager is expensive so
 * sharing it is usually the best option.
 * @author kleij - at - users.sourceforge.net
 * 
 */
public class FileSystemManagerFactory {

	private boolean share = true;
	private String managerClassName = "org.apache.commons.vfs2.impl.StandardFileSystemManager";

	private FileSystemManager sharedManager = null;
	
	public boolean isShare() {
		return share;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public String getManagerClassName() {
		return managerClassName;
	}

	public void setManagerClass(String managerClassName) {
		this.managerClassName = managerClassName;
	}

	/**
	 * Returns a FileSystemManager. If share is true (default) then a shared instance is
	 * returned, else a new one is created every time.
	 * 
	 * @return
	 * @throws FileSystemException
	 */
	public synchronized FileSystemManager getManager()
			throws FileSystemException {

		if (share) {
			if (sharedManager == null) {
				sharedManager = createManager(managerClassName);
			}
			return sharedManager;
		} else {
			return createManager(managerClassName);
		}

	}

	/**
	 * Creates a file system manager instance.
	 * 
	 * @param managerClassName
	 *            The specific manager implementation class name.
	 * @return The FileSystemManager.
	 * @throws FileSystemException
	 *             if an error occurs creating the manager.
	 */
	private FileSystemManager createManager(final String managerClassName)
			throws FileSystemException {
		try {
			// Create instance
			final Class mgrClass = Class.forName(managerClassName);
			final FileSystemManager mgr = (FileSystemManager) mgrClass
					.newInstance();

			try {
				// Initialize
				final Method initMethod = mgrClass.getMethod("init",
						(Class[]) null);
				initMethod.invoke(mgr, (Object[]) null);
			} catch (final NoSuchMethodException e) {
				// Ignore; don't initialize
			}

			return mgr;
		} catch (final InvocationTargetException e) {
			throw new FileSystemException("vfs/create-manager.error",
					managerClassName, e.getTargetException());
		} catch (final Exception e) {
			throw new FileSystemException("vfs/create-manager.error",
					managerClassName, e);
		}
	}

}
