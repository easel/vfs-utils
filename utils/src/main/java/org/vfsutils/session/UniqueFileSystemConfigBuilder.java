package org.vfsutils.session;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;


/**
 * Allows making the file system options unique so the file system will not 
 * be shared with other resolvers. You can only safely close the file system
 * with FileSystemManager.closeFileSystem(FileSystem) if the file system is 
 * not shared.
 * @author kleij - at - users.sourceforge.net
 *
 */
public class UniqueFileSystemConfigBuilder extends FileSystemConfigBuilder {

	private static final UniqueFileSystemConfigBuilder INSTANCE = new UniqueFileSystemConfigBuilder();
	
	public static UniqueFileSystemConfigBuilder getInstance() {
		return INSTANCE;
	}
	
	protected Class getConfigClass() {
		return UniqueFileSystemConfigBuilder.class;
	}
	
	public void setUniqueKey(FileSystemOptions opts) {
		this.setParam(opts, "uniquekey", new Object());
	}
	
	public Object getUniqueKey(FileSystemOptions opts) {
		return this.getParam(opts, "uniquekey");
	}

}
