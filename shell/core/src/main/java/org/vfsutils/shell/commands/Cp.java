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
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		args.assertSize(2);

		String srcPattern = args.getArgument(0);
		String destPath = args.getArgument(1);

		FileObject dest = engine.pathToFile(destPath);
		final FileObject[] files = engine.pathToFiles(srcPattern);

		CpOptions options = new CpOptions();
		options.verbose = args.hasFlag('v');
		options.preserveLastModified = !args.hasFlag('P');

		if (files.length == 0) {
			throw new IllegalArgumentException("File does not exist: "
					+ srcPattern);
		} else if (engine.pathIsPattern(srcPattern)) {
			cp(files, null, dest, options, engine);
		} else {
			FileObject src = files[0];
			cp(src, null, dest, options, engine);
		}

		engine.println("Copied " + options.cntDirs + " Folder(s), "
				+ options.cntFiles + " File(s) ");

	}

	public void cp(FileObject src, FileObject baseDir, FileObject dest,
			CpOptions options, Engine engine) throws IllegalArgumentException,
			CommandException, FileSystemException {
		if (src.getType().equals(FileType.FILE)) {
			if (dest.getType().equals(FileType.FOLDER)) {
				// resolve the target name
				FileObject imaginaryDest = resolve(src, baseDir, dest);
				// cpFile will not allow replacing a folder by a file
				cpFile(src, imaginaryDest, options, engine);
			} else {
				cpFile(src, dest, options, engine);
			}
		} else if (src.getType().equals(FileType.FOLDER)) {
			if (dest.getType().equals(FileType.FOLDER)) {
				// resolve the target name
				FileObject imaginaryDest = resolve(src, baseDir, dest);
				// cpFolder will not allow replacing a file by a folder
				cpFolder(src, imaginaryDest, options, engine);
			} else {
				// cpFolder will not allow replacing a file by a folder
				cpFolder(src, dest, options, engine);
			}
		}
	}

	public void cp(FileObject[] srcFiles, FileObject baseDir,
			FileObject destDir, CpOptions options, Engine engine)
			throws FileSystemException, IllegalArgumentException,
			CommandException {

		if (srcFiles.length > 1 && !destDir.getType().equals(FileType.FOLDER)) {
			throw new IllegalArgumentException("Copying multiple file but "
					+ destDir + " is not an existing folder");
		}

		for (int i = 0; i < srcFiles.length; i++) {
			FileObject srcFile = srcFiles[i];
			cp(srcFile, baseDir, destDir, options, engine);
		}
	}

	/**
	 * Copies a file to a target file. The target file may not exist yet.
	 * 
	 * @param srcFile
	 * @param destFile
	 * @param options
	 * @param engine
	 * @throws FileSystemException
	 *             if the source is not a file or if the target is a folder
	 */
	protected void cpFile(FileObject srcFile, FileObject destFile,
			CpOptions options, Engine engine) throws FileSystemException,
			CommandException {

		if (!srcFile.getType().equals(FileType.FILE)) {
			// Conflict
			throw new CommandException("Can not copy " + srcFile + " to "
					+ destFile);
		}

		// copyFrom is too aggressive and will destroy a folder to put a
		// file; not very user-friendly imho - I've lost some data this
		// way
		if (destFile.getType().equals(FileType.FOLDER)) {
			// Conflict
			throw new CommandException("Can not overwrite folder " + destFile
					+ " with file " + srcFile);
		}

		destFile.copyFrom(srcFile, Selectors.SELECT_SELF);
		options.cntFiles++;
		if (options.preserveLastModified
				&& srcFile.getFileSystem().hasCapability(
						Capability.GET_LAST_MODIFIED)
				&& destFile.getFileSystem().hasCapability(
						Capability.SET_LAST_MODIFIED_FILE)) {
			destFile.getContent().setLastModifiedTime(
					srcFile.getContent().getLastModifiedTime());
		}
		if (options.verbose) {
			engine.println("Copied file " + engine.toString(srcFile) + " to "
					+ engine.toString(destFile));
		}
	}

	/**
	 * Recursively copies a folder to a target folder
	 * 
	 * @param srcDir
	 * @param destDir
	 * @param options
	 * @param engine
	 * @throws FileSystemException
	 *             if the source is not a folder or if the target is a file
	 * @throws CommandException
	 */
	protected void cpFolder(FileObject srcDir, FileObject destDir,
			CpOptions options, Engine engine) throws FileSystemException,
			CommandException {

		if (!srcDir.getType().equals(FileType.FOLDER)) {
			// Conflict
			throw new CommandException("Can not copy " + srcDir + " to "
					+ destDir);
		}

		if (destDir.getType().equals(FileType.FILE)) {
			// Conflict
			throw new CommandException("Can not overwrite file " + destDir
					+ " with folder " + srcDir);
		}

		// copy the destDir
		destDir.copyFrom(srcDir, Selectors.SELECT_SELF);
		options.cntDirs++;
		if (options.preserveLastModified
				&& srcDir.getFileSystem().hasCapability(
						Capability.GET_LAST_MODIFIED)
				&& destDir.getFileSystem().hasCapability(
						Capability.SET_LAST_MODIFIED_FOLDER)) {
			destDir.getContent().setLastModifiedTime(
					srcDir.getContent().getLastModifiedTime());
		}
		if (options.verbose) {
			engine.println("Copied directory " + engine.toString(srcDir)
					+ " to " + engine.toString(destDir));
		}

		{
			// spider through the children
			FileObject[] srcChildren = srcDir.getChildren();

			for (int i = 0; i < srcChildren.length; i++) {
				FileObject srcChild = srcChildren[i];
				FileObject destChild = destDir.resolveFile(srcChild.getName()
						.getBaseName(), NameScope.CHILD);

				/*
				 * if (srcChild.getType().equals(FileType.FILE) &&
				 * destChild.getType().equals(FileType.FOLDER)){ // Conflict
				 * throw new CommandException("Can not convert file " + srcChild
				 * + " to folder " + destChild); }
				 * 
				 * if (srcChild.getType().equals(FileType.FOLDER) &&
				 * destChild.getType().equals(FileType.FILE)){ // Conflict throw
				 * new CommandException("Can not convert folder " + srcChild +
				 * " to file " + destChild); }
				 */

				// if src is a file
				if (srcChild.getType().equals(FileType.FILE)) {
					cpFile(srcChild, destChild, options, engine);
				}

				// if src is a folder
				if (srcChild.getType().equals(FileType.FOLDER)) {
					cpFolder(srcChild, destChild, options, engine);
				}

				// when done processing, release reference to child
				srcChildren[i] = null;
			}

		}
	}

	protected FileObject resolve(FileObject src, FileObject baseDir,
			FileObject dest) throws FileSystemException {

		if (baseDir == null) {
			return dest.resolveFile(src.getName().getBaseName());
		} else {
			// we create relative paths within the target folder
			String relativePath = baseDir.getName().getRelativeName(
					src.getName());
			return dest.resolveFile(relativePath, NameScope.DESCENDENT);
		}
	}

}
