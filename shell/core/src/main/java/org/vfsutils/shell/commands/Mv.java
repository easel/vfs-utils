package org.vfsutils.shell.commands;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Mv extends AbstractCommand implements CommandProvider {

	public Mv() {
		super("mv", new CommandInfo("Move a file", "<src> <dest>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		args.assertSize(2);

		String srcPath = args.getArgument(0);
		String destPath = args.getArgument(1);

		move(srcPath, destPath, engine);
	}

	protected void move(String srcPath, String destPath, Engine engine)
			throws FileSystemException, CommandException {
		final FileObject src = engine.pathToFile(srcPath);
		FileObject dest = engine.pathToFile(destPath);

		move(src, dest, engine);
	}

	protected void move(FileObject src, FileObject dest, Engine engine)
			throws FileSystemException, CommandException {

		if (!src.exists()) {
			throw new CommandException("Source " + engine.toString(src)
					+ " does not exist");
		}

		// when the object does not exist yet, its type will be IMAGINARY
		if (src.getType().equals(FileType.FILE)) {
			if (dest.getType().equals(FileType.FOLDER)) {
				FileObject imaginaryDest = dest.resolveFile(src.getName()
						.getBaseName());
				// moveTo is too aggressive and will destroy a folder to put a
				// file
				if (imaginaryDest.getType().equals(FileType.FOLDER)) {
					throw new CommandException(
							"A folder exists with the same name: "
									+ engine.toString(imaginaryDest));
				}
				src.moveTo(imaginaryDest);
			} else {
				src.moveTo(dest);
			}
		} else if (src.getType().equals(FileType.FOLDER)) {
			if (dest.getType().equals(FileType.FOLDER)) {

				if (src.equals(dest)) {
					throw new CommandException("Can not move "
							+ engine.toString(src) + " into itself");
				} else if (src.getName().isDescendent(dest.getName())) {
					throw new CommandException("Can not move "
							+ engine.toString(src) + " into its descendent "
							+ engine.toString(dest));
				}

				FileObject imaginaryDest = dest.resolveFile(src.getName()
						.getBaseName());
				// moveTo is too aggressive and will destroy the target folder
				// if it exists
				if (imaginaryDest.getType().equals(FileType.FILE)) {
					throw new CommandException(
							"A file exists with the same name: "
									+ engine.toString(imaginaryDest));
				} else if (imaginaryDest.getType().equals(FileType.FOLDER)) {
					throw new CommandException(
							"A folder exists with the same name: "
									+ engine.toString(imaginaryDest));
				}
				src.moveTo(imaginaryDest);
			} else if (dest.getType().equals(FileType.FILE)) {
				throw new CommandException("A file exists with the same name: "
						+ engine.toString(dest));
			} else {
				src.moveTo(dest);
			}
		}

	}

}
