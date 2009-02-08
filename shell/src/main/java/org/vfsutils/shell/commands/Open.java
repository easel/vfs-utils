package org.vfsutils.shell.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Open extends AbstractOpenClose {

	public Open() {
		super("open", new CommandInfo("Open a filesystem", "[[-upd] <uri>]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		if (args.size() == 0) {
			listOpen(engine);
		} else {
			String path = args.getArgument(0);
			boolean askUsername = args.hasFlag("u");
			boolean askPassword = args.hasFlag("p");
			boolean askDomain = args.hasFlag("d");
			open(path, askUsername, askPassword, askDomain, engine);
		}
	}

	protected void listOpen(Engine engine) throws FileSystemException {
		List openFs = getOpenFs(engine);
		for (int i = 0; i < openFs.size(); i++) {
			FileName name = (FileName) openFs.get(i);
			engine.println(" " + (i > 8 ? "" : " ") + "[" + (i + 1) + "] "
					+ name.getURI());
		}
	}

	protected void open(String path, boolean askUsername, boolean askPassword,
			boolean askDomain, Engine engine) throws FileSystemException, CommandException {

		FileObject file;
		if (path.indexOf("://") == -1) {
			FileObject layeredFile = engine.pathToFile(path);
			file = engine.getMgr().createFileSystem(layeredFile);
		} else {
			FileSystemOptions opts = new FileSystemOptions();

			if (askUsername || askPassword) {
				BufferedReader buf = new BufferedReader(engine.getConsole()
						.getIn());

				String username = null, password = null, domain = null;
				try {
					if (askUsername) {
						engine.print("username > ");
						username = buf.readLine();
					}
					if (askPassword) {
						engine.print("password > ");
						password = buf.readLine();
					}
					if (askDomain) {
						engine.println("domain > ");
						domain = buf.readLine();
					}
				} catch (IOException e) {
					throw new CommandException(e);
				}

				StaticUserAuthenticator auth = new StaticUserAuthenticator(
						domain, username, password);

				DefaultFileSystemConfigBuilder.getInstance()
						.setUserAuthenticator(opts, auth);
			}
			file = engine.getMgr().resolveFile(path, opts);
		}

		// same as the cd command
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist "
					+ engine.toString(file));
		}

		if (file.getType().equals(FileType.FILE)) {
			throw new IllegalArgumentException("Not a directory");
		}

		FileName root = file.getName().getRoot();
		List openFs = getOpenFs(engine);

		// If the new fs is not layered add the current fs in the list to go to
		// when the new fs is closed
		if (openFs.size() == 0 && file.getFileSystem().getParentLayer() == null) {
			openFs.add(engine.getCwd().getName());
		}

		// add the new fs to the list if it is not there yet
		if (!openFs.contains(root)) {
			openFs.add(root);
		}

		engine.getContext().setCwd(file);

		engine.println("Opened " + engine.toString(root));
		engine.println("Current folder is " + engine.toString(file));

	}

}
