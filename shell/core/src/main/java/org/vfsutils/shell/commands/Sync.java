package org.vfsutils.shell.commands;

import java.util.LinkedList;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.Selectors;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Sync extends AbstractCommand {
	
	protected boolean compareDate = true;
	protected boolean compareSize = false;
	protected boolean delete = false;
	protected boolean preserveLastModified = true;
	
	public Sync() {
		super("sync", "Synchronize two locations", "<fromPath> <toPath> [--delete] [-sdP]");
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(2);
		
		FileObject srcFileObject = engine.pathToFile(args.getArgument(0));
		FileObject destFileObject = engine.pathToFile(args.getArgument(1));
		
		this.delete = args.hasFlag("delete");
		this.compareSize = args.hasFlag("s");
		this.compareDate = args.hasFlag("d") || !compareSize;
		this.preserveLastModified = !args.hasFlag("P");
		
		sync(srcFileObject, destFileObject, engine);
		
	}
	
	/**
	 * Synchronizes the source with the target. The source must always exist; the destination can be imaginary
	 * If the source is a file, the destination can be a file or a folder
	 * @param src
	 * @param dest
	 * @param engine
	 * @throws CommandException
	 * @throws FileSystemException
	 */
	public void sync(FileObject src, FileObject dest, Engine engine) throws IllegalArgumentException, CommandException, FileSystemException {
		if (src.getType().equals(FileType.FILE)) {
			if (dest.getType().equals(FileType.FOLDER)) {
				FileObject imaginaryDest = dest.resolveFile(src.getName().getBaseName());
				syncFiles(src, imaginaryDest, engine);
			}
			else {				
				syncFiles(src, dest, engine);
			}
		}
		else if (src.getType().equals(FileType.FOLDER)) {
			if (dest.getType().equals(FileType.FILE)) {
				throw new IllegalArgumentException("You cannot synchronize a folder with a file");
			}
			else {
				syncDirs(src, dest, engine);
			}
		}
	}
	
	protected void syncFiles(FileObject srcFile, FileObject destFile, Engine engine) throws CommandException, FileSystemException {
		if (!areSame(srcFile, destFile)) {
			destFile.copyFrom(srcFile, Selectors.SELECT_SELF);
			 if (preserveLastModified  && 
		                srcFile.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED) &&
		                destFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
				 destFile.getContent().setLastModifiedTime(srcFile.getContent().getLastModifiedTime());
		     }
		}
	}
	
	protected void syncDirs(FileObject srcDir, FileObject destDir, Engine engine) throws CommandException, FileSystemException {
		if (!destDir.exists()) {
			//copy all
			destDir.copyFrom(srcDir, Selectors.SELECT_SELF_AND_CHILDREN);
		}
		else {
			//spider through the children
			FileObject[] srcChildren = srcDir.getChildren();
			FileObject[] tmpDestChildren = destDir.getChildren();
						
			//create list of destChildren
			java.util.List destChildren = new LinkedList();
			for (int i=0; i<tmpDestChildren.length; i++) {
				destChildren.add(tmpDestChildren[i]);
			}
			
			FileObject srcChild = null;
			FileObject destChild = null;
			
			for (int i=0; i<srcChildren.length; i++){
				srcChild = srcChildren[i];
				destChild = destDir.resolveFile(srcChild.getName().getBaseName(), NameScope.CHILD);
				destChildren.remove(destChild);
			
				//if delete, remove destChild in case of type conflict
				if (delete) {
					if (srcChild.getType().equals(FileType.FILE) & destChild.getType().equals(FileType.FOLDER)) {
						destChild.delete(Selectors.SELECT_SELF_AND_CHILDREN);
					}
					else if (srcChild.getType().equals(FileType.FOLDER) && destChild.getType().equals(FileType.FILE)) {
						destChild.delete();
					}
				}				
				
				//both are files (dest can be imaginary)
				if (!srcChild.getType().equals(FileType.FOLDER) && !destChild.getType().equals(FileType.FOLDER)) {				
					syncFiles(srcChild, destChild, engine);
				}
				
				//both are folders (dest can be imaginary)
				if (!srcChild.getType().equals(FileType.FILE) && !destChild.getType().equals(FileType.FILE)){
					syncDirs(srcChild, destChild, engine);
				}
				
				
			}
			
			if (delete) {
				for (int i=0; i<destChildren.size(); i++) {
					FileObject remainingChild = (FileObject) destChildren.get(i);
					remainingChild.delete(Selectors.SELECT_SELF_AND_CHILDREN);
				}
			}
			
		}
	}

	protected boolean areSame(FileObject fileA, FileObject fileB) throws FileSystemException {
		
		if (!fileB.exists()) {
			return false;
		}
		
		boolean sameDate = false;
		if (compareDate) {
			sameDate = (fileA.getContent().getLastModifiedTime() == fileB.getContent().getLastModifiedTime());
		}
		
		boolean sameSize = false;
		if (compareSize) {
			sameSize = fileA.getContent().getSize() == fileB.getContent().getSize();
		}
		
		if (compareDate && compareSize) return sameSize && sameDate;
		else if (compareDate) return sameDate;
		else if (compareSize) return sameSize;
		else return true;
	}
	
}
