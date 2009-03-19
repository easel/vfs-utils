package org.vfsutils.shell.commands;

import java.math.BigInteger;
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
	
	public class SyncOptions {
		public boolean compareDate = true;
		public boolean compareSize = false;
		public boolean compareMd5 = false;
		public boolean delete = false;
		public boolean preserveLastModified = true;
		public boolean verbose = false;
		public boolean dryRun = false;
		
		protected int cntFiles = 0;
		protected int cntSyncFiles = 0;
		protected int cntDirs = 0;
		protected int cntSyncDirs = 0;
		protected int cntRemoved = 0;
	}

	protected org.vfsutils.Md5 md5Helper;
	
	public Sync() {
		super("sync", "Synchronize two locations", "<fromPath> <toPath> [--delete] [--dry-run] [-sdmPv]");
		this.md5Helper = new org.vfsutils.Md5();
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(2);
		
		FileObject srcFileObject = engine.pathToFile(args.getArgument(0));
		FileObject destFileObject = engine.pathToFile(args.getArgument(1));
		
		SyncOptions options = new SyncOptions();
		
		options.delete = args.hasFlag("delete");
		options.compareSize = args.hasFlag('s');
		options.compareMd5 = args.hasFlag('m');
		options.compareDate = args.hasFlag('d') || !(options.compareSize || options.compareMd5);
		options.preserveLastModified = !args.hasFlag('P');
		options.verbose = args.hasFlag('v');
		options.dryRun = args.hasFlag("dry-run");
		
		sync(srcFileObject, destFileObject, options, engine);
		
		engine.println((options.dryRun?"[Dry run] ":"") + "Synchronized " + options.cntSyncDirs + " of " + options.cntDirs + " Folder(s), " 
				+ options.cntSyncFiles + " of " + options.cntFiles + " File(s), Removed " + options.cntRemoved + " items");
		
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
	public void sync(FileObject src, FileObject dest, SyncOptions options, Engine engine) throws IllegalArgumentException, CommandException, FileSystemException {
		if (src.getType().equals(FileType.FILE)) {
			if (dest.getType().equals(FileType.FOLDER)) {
				FileObject imaginaryDest = dest.resolveFile(src.getName().getBaseName());
				syncFiles(src, imaginaryDest, options, engine);
			}
			else {				
				syncFiles(src, dest, options, engine);
			}
		}
		else if (src.getType().equals(FileType.FOLDER)) {
			if (dest.getType().equals(FileType.FILE)) {
				throw new IllegalArgumentException("You cannot synchronize a folder with a file");
			}
			else {
				syncDirs(src, dest, options, engine);
			}
		}
	}
	
	protected void syncFiles(FileObject srcFile, FileObject destFile, SyncOptions options, Engine engine) throws CommandException, FileSystemException {
		
		options.cntFiles++;
		if (!areSame(srcFile, destFile, options)) {
			if (!options.dryRun) {
				destFile.copyFrom(srcFile, Selectors.SELECT_SELF);
				
				if (options.preserveLastModified  && 
			                srcFile.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED) &&
			                destFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
					 destFile.getContent().setLastModifiedTime(srcFile.getContent().getLastModifiedTime());
			    }
			}
			if (options.verbose) {
				 engine.println("Copied file " + engine.toString(srcFile) + " to " + engine.toString(destFile));
			}
			options.cntSyncFiles++;
		}
	}
	
	protected void syncDirs(FileObject srcDir, FileObject destDir, SyncOptions options, Engine engine) throws CommandException, FileSystemException {
		
		java.util.List destChildren = new LinkedList();
		options.cntDirs++;
		if (!destDir.exists()) {

			if (!options.dryRun) {
				//create the destDir
				destDir.copyFrom(srcDir, Selectors.SELECT_SELF);
				options.cntSyncDirs++;
				if (options.preserveLastModified  && 
						srcDir.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED) &&
						destDir.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FOLDER)) {
					destDir.getContent().setLastModifiedTime(srcDir.getContent().getLastModifiedTime());
				}
			}
			if (options.verbose) {
				engine.println("Copied directory " + engine.toString(srcDir) + " to " + engine.toString(destDir));
			}
		}
		else {
			FileObject[] tmpDestChildren = destDir.getChildren();
			//create list of destChildren			
			for (int i=0; i<tmpDestChildren.length; i++) {
				destChildren.add(tmpDestChildren[i]);
			}
		}
		
		{
			//spider through the children
			FileObject[] srcChildren = srcDir.getChildren();
			
			for (int i=0; i<srcChildren.length; i++){
				FileObject srcChild = srcChildren[i];
				FileObject destChild = destDir.resolveFile(srcChild.getName().getBaseName(), NameScope.CHILD);
				destChildren.remove(destChild);
			
				//if delete, remove destChild in case of type conflict
				if (options.delete) {
					if (srcChild.getType().equals(FileType.FILE) & destChild.getType().equals(FileType.FOLDER)) {
						if (!options.dryRun) destChild.delete(Selectors.SELECT_SELF_AND_CHILDREN);
						options.cntRemoved++;
						if (options.verbose) {
							engine.println("Removed " + engine.toString(destChild) + "because of a type conflict");
						}
					}
					else if (srcChild.getType().equals(FileType.FOLDER) && destChild.getType().equals(FileType.FILE)) {
						if (!options.dryRun) destChild.delete();
						options.cntRemoved++;
						if (options.verbose) {
							engine.println("Removed " + engine.toString(destChild) + "because of a type conflict");
						}
					}
				}				
				
				//both are files (dest can be imaginary)
				if (!srcChild.getType().equals(FileType.FOLDER) && !destChild.getType().equals(FileType.FOLDER)) {				
					syncFiles(srcChild, destChild, options, engine);
				}
				
				//both are folders (dest can be imaginary)
				if (!srcChild.getType().equals(FileType.FILE) && !destChild.getType().equals(FileType.FILE)){
					syncDirs(srcChild, destChild, options, engine);
				}
				
				//when done processing, release reference to child
				srcChildren[i]=null;				
				
			}
			
			if (options.delete) {
				for (int i=0; i<destChildren.size(); i++) {
					FileObject remainingChild = (FileObject) destChildren.get(i);
					if (!options.dryRun) {
						options.cntRemoved += remainingChild.delete(Selectors.SELECT_SELF_AND_CHILDREN);
					}
					else {
						options.cntRemoved += remainingChild.findFiles(Selectors.SELECT_SELF_AND_CHILDREN).length;
					}
					if (options.verbose){
						engine.println("Removed " + engine.toString(remainingChild));
					}
				}
			}
			
		}
	}

	
	protected boolean areSame(FileObject fileA, FileObject fileB, SyncOptions options) throws FileSystemException {
		
		if (!fileB.exists()) {
			return false;
		}
		
		boolean sameDate = true;
		if (options.compareDate) {
			sameDate = (fileA.getContent().getLastModifiedTime() <= fileB.getContent().getLastModifiedTime());
		}
		
		boolean sameSize = true;
		if (options.compareSize) {
			sameSize = fileA.getContent().getSize() == fileB.getContent().getSize();
		}
		
		boolean sameMd5 = true;
		if (options.compareMd5) {
			
			BigInteger md5A = md5Helper.calculateMd5(fileA);
			BigInteger md5B = md5Helper.calculateMd5(fileB);
			sameMd5 = md5A.equals(md5B);					
		}
		
		return sameSize && sameDate && sameMd5;		
	}

	public void setMd5Helper(org.vfsutils.Md5 md5Helper) {
		this.md5Helper = md5Helper;
	}
}
