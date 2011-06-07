package org.vfsutils.spider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

/**
 * Abstract base class for selectors that do not collect results, but that
 * execute something on the file while traversing. This avoids keeping large
 * lists of file objects in memory and thus scales better. It relies on an
 * underlying selector for determining whether to visit the file.
 * 
 * @author kleij - at - users.sourceforge.net
 * 
 */
public abstract class VisitingSelector implements FileSelector {

	protected FileSelector selector;

	/**
	 * The given selector is used to determine whether to include files and
	 * whether to traverse descendants.
	 * 
	 * @param wrappedSelector
	 */
	public VisitingSelector(FileSelector wrappedSelector) {
		this.selector = wrappedSelector;
	}

	/**
	 * If the underlying selector includes the file then visitFile(FileObject)
	 * is called.
	 * 
	 * @return false, because the file should not be collected
	 */
	public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
		if (selector.includeFile(fileInfo)) {
			visitFile(fileInfo.getFile());
		}
		// never collect the results
		return false;
	}

	public boolean traverseDescendents(FileSelectInfo fileInfo)
			throws Exception {
		return this.selector.traverseDescendents(fileInfo);
	}

	/**
	 * Execute something on the file. This method is only called when the
	 * underlying selector includes the file
	 * 
	 * @param fileObject
	 *            can be a file or a folder
	 * @throws Exception
	 */
	public abstract void visitFile(FileObject fileObject) throws Exception;

}
