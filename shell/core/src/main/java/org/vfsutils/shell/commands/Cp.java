package org.vfsutils.shell.commands;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.Selectors;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Cp extends AbstractCommand implements CommandProvider {

	public class CpOptions {
		public boolean preserveLastModified = true;
		public boolean verbose = false;
		
		protected int cntFiles = 0;
		protected int cntDirs = 0;
	}
	
	public Cp() {
		super("cp", new CommandInfo("Copies an item", "<src> <dest> [-Pv]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException, FileSystemException {
		
		args.assertSize(2);

		String srcPattern = args.getArgument(0);
		String destPath = args.getArgument(1);
		
        FileObject dest = engine.pathToFile(destPath);        
        final FileObject[] files = engine.pathToFiles(srcPattern);
        
        CpOptions options = new CpOptions();
        options.verbose = args.hasFlag('v');
        options.preserveLastModified = !args.hasFlag('P');

		if (files.length == 0) {
			throw new IllegalArgumentException("File does not exist: " + srcPattern);
		}
		else if (files.length == 1) {
			FileObject src = files[0];			
        	cp(src, dest, options, engine);
		}
		else {
			cp(files, dest, options, engine);			
        }    
		
		engine.println("Copied " + options.cntDirs + " Folder(s), "+ options.cntFiles + " File(s) ");

	}
		
	
	public void cp(FileObject src, FileObject dest, CpOptions options, Engine engine) throws IllegalArgumentException, CommandException, FileSystemException {
		if (src.getType().equals(FileType.FILE)) {
			if (dest.getType().equals(FileType.FOLDER)) {
				FileObject imaginaryDest = dest.resolveFile(src.getName().getBaseName());
				//copyFrom is too aggressive and will destroy a folder to put a file; not very user-friendly imo - I've lost some data this way
				if (imaginaryDest.getType().equals(FileType.FOLDER)) {
					throw new CommandException("You cannot copy a file as folder " + engine.toString(imaginaryDest));
				}
				cpFiles(src, imaginaryDest, options, engine);
			}
			else {				
				cpFiles(src, dest, options, engine);
			}
		}
		else if (src.getType().equals(FileType.FOLDER)) {
			if (dest.getType().equals(FileType.FILE)) {
				throw new IllegalArgumentException("You cannot copy a folder with to file");
			}
			else {
				cpDirs(src, dest, options, engine);
			}
		}
	}

	public void cp(FileObject[] srcFiles, FileObject destDir, CpOptions options, Engine engine) throws FileSystemException, IllegalArgumentException, CommandException {
		
		if (!destDir.exists() || destDir.getType() == FileType.FOLDER) {
			throw new IllegalArgumentException("You cannot copy multiple files to one location");
		}
		
		for (int i = 0; i < srcFiles.length; i++) {
			FileObject srcFile = srcFiles[i];
			//we do not create relative paths within the target folder
			FileObject destFile = destDir.resolveFile(srcFile.getName().getBaseName());
			
			cp(srcFile, destFile, options, engine);
		}
	}
	
	public void cpFiles(FileObject srcFile, FileObject destFile, CpOptions options, Engine engine) throws FileSystemException {
		
		destFile.copyFrom(srcFile, Selectors.SELECT_SELF);
		options.cntFiles++;
		if (options.preserveLastModified  && 
                srcFile.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED) &&
                destFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
			destFile.getContent().setLastModifiedTime(srcFile.getContent().getLastModifiedTime());
		}
		if (options.verbose) {
			engine.println("Copied file " + engine.toString(srcFile) + " to " + engine.toString(destFile));
		}
	}
	
	public void cpDirs(FileObject srcDir, FileObject destDir, CpOptions options, Engine engine) throws FileSystemException {
		
		//copy the destDir
		destDir.copyFrom(srcDir, Selectors.SELECT_SELF);
		options.cntDirs++;
		if (options.preserveLastModified  && 
				srcDir.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED) &&
				destDir.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FOLDER)) {
			destDir.getContent().setLastModifiedTime(srcDir.getContent().getLastModifiedTime());
		}
		if (options.verbose) {
			engine.println("Copied directory " + engine.toString(srcDir) + " to " + engine.toString(destDir));
		}
		
		
		{
			//spider through the children
			FileObject[] srcChildren = srcDir.getChildren();
			
			for (int i=0; i<srcChildren.length; i++){
				FileObject srcChild = srcChildren[i];
				FileObject destChild = destDir.resolveFile(srcChild.getName().getBaseName(), NameScope.CHILD);
			
				//both are files (dest can be imaginary)
				if (!srcChild.getType().equals(FileType.FOLDER) && !destChild.getType().equals(FileType.FOLDER)) {				
					cpFiles(srcChild, destChild, options, engine);
				}
				
				//both are folders (dest can be imaginary)
				if (!srcChild.getType().equals(FileType.FILE) && !destChild.getType().equals(FileType.FILE)){
					cpDirs(srcChild, destChild, options, engine);
				}
				
				//when done processing, release reference to child
				srcChildren[i]=null;				
			}
			
		}
	}
	
}
