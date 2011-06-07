package org.vfsutils.shell.commands;

import java.math.BigInteger;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.Selectors;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;

public class Compare extends AbstractCommand {
	
	public class CompareOptions {
		public boolean compareDate = true;
		public boolean compareDateNewer = true;
		public boolean compareSize = false;
		public boolean compareMd5 = false;
		public boolean purge = true;
		public boolean verbose = false;		
		
		public FileObject srcBase = null;
		public FileObject destBase = null;		
		
		protected int cntFiles = 0;
		protected int cntMissingFiles = 0;
		protected int cntChangedFiles = 0;
		protected int cntDirs = 0;
		protected int cntMissingDirs = 0;
		protected int cntMissingDirItems = 0;
		protected int cntRemoved = 0;
		
	}

	protected org.vfsutils.Md5 md5Helper;
	
	public Compare() {
		super("compare", "Compares two locations", "<fromPath> <toPath> [-sdmNv]");
		this.md5Helper = new org.vfsutils.Md5();
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		args.assertSize(2);
		
		FileObject srcFileObject = engine.pathToExistingFile(args.getArgument(0));
		FileObject destFileObject = engine.pathToExistingFile(args.getArgument(1));
		
		CompareOptions options = new CompareOptions();
		
		options.compareSize = args.hasFlag('s');
		options.compareMd5 = args.hasFlag('m');
		options.compareDate = args.hasFlag('d') || !(options.compareSize || options.compareMd5);
		options.compareDateNewer = !args.hasFlag("N");
		options.verbose = args.hasFlag('v');
		
		options.srcBase = srcFileObject;
		options.destBase = destFileObject;
		
		if (options.verbose) {
			engine.println("Comparing using " + (options.compareSize?"size ":"") + (options.compareDate?(options.compareDateNewer?"newer":"strict") + " date ":"") + (options.compareMd5?"md5 ":""));
		}
		
		sync(srcFileObject, destFileObject, options, engine);
		
		StringBuffer result = new StringBuffer(100);
		result.append("Difference: ");
		
		if (options.cntDirs>0){
			result.append(options.cntDirs + " Folder(s)");
				
			if (options.cntMissingDirs>0) {
				result.append(" - missing " + options.cntMissingDirs + " containing " + options.cntMissingDirItems + " items, "); 
			}		
			else {
				result.append(", ");
			}
		}
		result.append(options.cntFiles + " File(s) - " + options.cntChangedFiles + " different");
		
		if (options.cntMissingFiles > 0) {
			result.append(" and " + options.cntMissingFiles + " missing");
		}
		
		if (options.cntRemoved>0) {
			result.append(", " + options.cntRemoved + " conflicting or superfluous items");
		}
		
		engine.println(result.toString());
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
	public void sync(FileObject src, FileObject dest, CompareOptions options, Engine engine) throws IllegalArgumentException, CommandException, FileSystemException {
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
				throw new IllegalArgumentException("You cannot compare a folder with a file");
			}
			else {
				// do not count the starting dir
				options.cntDirs--;
				syncDirs(src, dest, options, engine);
			}
		}
	}
	
	protected void syncFiles(FileObject srcFile, FileObject destFile, CompareOptions options, Engine engine) throws CommandException, FileSystemException {
		
		options.cntFiles++;
		
		if (!destFile.exists()) {
			syncFileAction(srcFile, destFile, options, engine);
			options.cntMissingFiles++;
		}
		else if (!areSame(srcFile, destFile, options)) {
			syncFileAction(srcFile, destFile, options, engine);				
			options.cntChangedFiles++;
		}
	}
	
	

	protected void syncDirs(FileObject srcDir, FileObject destDir, CompareOptions options, Engine engine) throws CommandException, FileSystemException {
		
		java.util.List destChildren = new LinkedList();
		options.cntDirs++;
		if (!destDir.exists()) {
			options.cntMissingDirItems += syncDirAction(srcDir, destDir, options, engine);			
			options.cntMissingDirs++;
		}
		else {
			FileObject[] tmpDestChildren = destDir.getChildren();
			//create list of destChildren			
			for (int i=0; i<tmpDestChildren.length; i++) {
				destChildren.add(tmpDestChildren[i]);
			}
		
			//spider through the children
			FileObject[] srcChildren = srcDir.getChildren();
			
			for (int i=0; i<srcChildren.length; i++){
				FileObject srcChild = srcChildren[i];
				FileObject destChild = destDir.resolveFile(srcChild.getName().getBaseName(), NameScope.CHILD);
				destChildren.remove(destChild);
			
				//if purge is allowed, remove destChild in case of type conflict
				if (options.purge) {
					if (destChild.exists() && !srcChild.getType().equals(destChild.getType())) {
						options.cntRemoved += typeConflictAction(srcChild, destChild, options, engine);						
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
			
			if (options.purge) {
				for (int i=0; i<destChildren.size(); i++) {
					FileObject remainingChild = (FileObject) destChildren.get(i);
					options.cntRemoved += remainingChildAction(remainingChild, options, engine);
				}
			}
			
		}
	}

	
	protected int remainingChildAction(FileObject remainingChild,
			CompareOptions options, Engine engine) throws FileSystemException {
		
		int remaining = remainingChild.findFiles(Selectors.SELECT_ALL).length;
		
		if (options.verbose){
			engine.println("Superfluous " + engine.toString(remainingChild));
		}
		
		return remaining;
	}

	protected int typeConflictAction(FileObject srcChild, FileObject destChild,
			CompareOptions options, Engine engine) throws FileSystemException {
		
		int conflicting = destChild.findFiles(Selectors.SELECT_ALL).length;
		
		if (options.verbose) {
			engine.println("Type conflict between " + engine.toString(srcChild) + " and " + engine.toString(destChild) + " (" + conflicting + " items)");
		}
		
		return conflicting;
	}

	protected int syncDirAction(FileObject srcDir, FileObject destDir,
			CompareOptions options, Engine engine) throws FileSystemException {
		
		int missing = srcDir.findFiles(Selectors.EXCLUDE_SELF).length;
		if (options.verbose) {
			engine.println("Directory " + engine.toString(destDir) + " does not exist (" + missing + " items)");
		}
		return missing;
	}

	protected void syncFileAction(FileObject srcFile, FileObject destFile,
			CompareOptions options, Engine engine) throws FileSystemException {
		
		if (options.verbose) {
			if (!destFile.exists()) {
				engine.println("File " + engine.toString(destFile) + " does not exist");
			}
			else {
				engine.println("Difference between " + engine.toString(srcFile) + " and " + engine.toString(destFile));
			}
		}
	
	}
	
	protected boolean areSame(FileObject fileA, FileObject fileB, CompareOptions options) throws FileSystemException {
		
		if (!fileB.exists()) {
			return false;
		}
		
		boolean sameDate = true;
		if (options.compareDate) {
			if (options.compareDateNewer) {
				sameDate = (fileA.getContent().getLastModifiedTime() <= fileB.getContent().getLastModifiedTime());
			}
			else {
				sameDate = (fileA.getContent().getLastModifiedTime() == fileB.getContent().getLastModifiedTime());
			}
			
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
