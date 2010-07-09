package org.vfsutils.shell.commands;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.UriParser;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.Arguments.Option;
import org.vfsutils.shell.Arguments.OptionMap;

public class Open extends AbstractOpenClose {

	public Open() {
		super("open", new CommandInfo("Open a filesystem",
				"[[-upd] [--virtual] [--<option>=<value>]* <uri> | ~<idx>]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		if (args.size() == 0) {
			listOpen(engine);
		} else if (args.getArgument(0).startsWith("~")
				&& args.getArgument(0).length() > 1) {
			int i = Integer.parseInt(args.getArgument(0).substring(1));
			cd(i, engine);
		} else {
			String path = args.getArgument(0);
			boolean askUsername = args.hasFlag("u");
			boolean askPassword = args.hasFlag("p");
			boolean askDomain = args.hasFlag("d");
			boolean virtual = args.hasFlag("virtual");
			Map options = transformOptions(args.getOptions());
			open(path, askUsername, askPassword, askDomain, virtual, options,
					engine);
		}
	}

	protected void listOpen(Engine engine) throws FileSystemException {
		List openFs = getOpenFs(engine);
		for (int i = 0; i < openFs.size(); i++) {
			FileObject root = (FileObject) openFs.get(i);
			engine.println(" " + (i > 8 ? "" : " ") + "[" + (i + 1) + "] "
					+ engine.toString(root.getName()));
		}
	}

	public void cd(int i, Engine engine) throws CommandException {
		List openFs = getOpenFs(engine);
		if (i < 1 || i > openFs.size()) {
			throw new CommandException("Invalid index given: " + i);
		}
		FileObject file = (FileObject) openFs.get(i - 1);

		// change the working dir
		engine.getContext().setCwd(file);
		engine.println("Current folder is " + engine.toString(file));

	}

	public void open(String path, String username, String password,
			String domain, boolean virtual, Map options, Engine engine)
			throws FileSystemException, CommandException {

		FileObject file = resolvePath(path, username, password, domain,
				virtual, options, engine);

		open(file, engine);
	}

	public void open(String path, boolean askUsername, boolean askPassword,
			boolean askDomain, boolean virtual, Map options, Engine engine)
			throws FileSystemException, CommandException {

		FileObject file = resolvePath(path, askUsername, askPassword,
				askDomain, virtual, options, engine);

		open(file, engine);
	}

	public void open(FileObject file, Engine engine)
			throws FileSystemException, CommandException {

		// same as the cd command
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist "
					+ engine.toString(file));
		}

		if (file.getType().equals(FileType.FILE)) {
			throw new IllegalArgumentException("Not a directory");
		}

		FileObject root = file.getFileSystem().getRoot();
		List openFs = getOpenFs(engine);

		// When the fs is closed we should go back to something. For a layered
		// fs this
		// is the parent layer. For others we take the previously opened fs. In
		// case
		// this is the first fs to open the current directory is put as
		// fallback.
		if (openFs.size() == 0 && file.getFileSystem().getParentLayer() == null) {
			openFs.add(engine.getCwd());
		}

		// add the new fs to the list if it is not there yet
		if (!openFs.contains(root)) {
			openFs.add(root);
		}

		// change the working dir
		engine.getContext().setCwd(file);

		engine.println("Opened " + engine.toString(root));
		engine.println("Current folder is " + engine.toString(file));

	}

	public FileObject resolvePath(String path, String username,
			String password, String domain, boolean virtual, Map options,
			Engine engine) throws FileSystemException, CommandException {

		FileObject file;
		if (path.indexOf("://") == -1) {
			FileObject layeredFile = engine.pathToFile(path);

			if (virtual) {
				file = engine.getMgr().createVirtualFileSystem(layeredFile);
			} else {
				file = engine.getMgr().createFileSystem(layeredFile);
			}

		} else {
			FileSystemOptions opts = new FileSystemOptions();

			if (username != null || password != null || domain != null) {

				StaticUserAuthenticator auth = new StaticUserAuthenticator(
						domain, username, password);

				DefaultFileSystemConfigBuilder.getInstance()
						.setUserAuthenticator(opts, auth);
			}

			if (options != null && options.size() > 0) {
				String scheme = UriParser.extractScheme(path);

				FileSystemConfigBuilder configBuilder = engine.getMgr()
						.getFileSystemConfigBuilder(scheme);

				if (configBuilder != null) {

					Class cbClass = configBuilder.getClass();
					BeanInfo binfo;
					try {
						binfo = java.beans.Introspector.getBeanInfo(cbClass);
					} catch (IntrospectionException e1) {
						throw new CommandException(e1);
					}

					MethodDescriptor[] meths = binfo.getMethodDescriptors();

					for (int i = 0; i < meths.length; i++) {
						MethodDescriptor m = meths[i];

						// check if it exists in the arguments
						String name = m.getName();
						if (name.startsWith("set") && name.length() > 3) {
							String optionName = name.substring(3, 4)
									.toLowerCase()
									+ name.substring(4);

							if (options.containsKey(optionName)) {
								Method method = m.getMethod();
								try {
									method.invoke(configBuilder, new Object[] {
											opts, options.get(optionName) });
								} catch (Exception e) {
									throw new CommandException(e);
								}
							}
						}
					}
				}
			}

			file = engine.getMgr().resolveFile(path, opts);

			if (virtual) {
				file = engine.getMgr().createVirtualFileSystem(file);
			}
		}
		return file;
	}

	protected FileObject resolvePath(String path, boolean askUsername,
			boolean askPassword, boolean askDomain, boolean virtual,
			Map options, Engine engine) throws FileSystemException,
			CommandException {

		String username = null, password = null, domain = null;

		// only read input for full URLs
		if (path.indexOf("://") > -1
				&& (askUsername || askPassword || askDomain)) {

			try {
				BufferedReader buf = new BufferedReader(engine.getConsole()
						.getIn());

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

		}

		return resolvePath(path, username, password, domain, virtual, options,
				engine);

	}

	protected Map transformOptions(OptionMap options) {
		Map result = new TreeMap();
		Iterator iterator = options.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Option option = (Option) options.get(key);
			result.put(option.getName(), option.getValue());
		}
		return result;
	}

}
