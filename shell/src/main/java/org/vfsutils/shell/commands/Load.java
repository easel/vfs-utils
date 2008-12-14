package org.vfsutils.shell.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Load extends AbstractCommand implements CommandProvider {

	public Load() {
		super("load", new CommandInfo("Load a script", "[-c] <path>"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
		
		args.assertSize(1);

		if (args.hasFlag("c")) {
			call(args, engine);
		}
		else {
			load(args, engine);
		} 
	}

	public void load(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {

		String path = (String) args.getArguments().get(0);
		
		final FileObject file = engine.pathToFile(path);
		load(file, engine, true);
	}

	public void load(final FileObject file, Engine engine, boolean haltOnError)
			throws FileSystemException {

		final InputStream in = file.getContent().getInputStream();

		boolean prevHaltOnError = engine.isHaltOnError();
		boolean prevEchoOn = engine.isEchoOn();
		try {
			engine.setHaltOnError(haltOnError);
			engine.setEchoOn(true);
			engine.load(new InputStreamReader(in));
		} catch (Exception e) {
			throw new FileSystemException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
				;
			}
			engine.setHaltOnError(prevHaltOnError);
			engine.setEchoOn(prevEchoOn);
		}

	}

	public void call(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {
		
		String path = (String) args.getArguments().get(0);
		final FileObject file = engine.pathToFile(path);
		call(file, engine);
	}

	/**
	 * Executes the given script but does not change the current context
	 * 
	 * @param file
	 * @param engine
	 * @throws FileSystemException
	 */
	public void call(final FileObject file, Engine engine)
			throws FileSystemException {

		final InputStream in = file.getContent().getInputStream();

		try {
			Engine newEngine = new Engine(engine.getConsole(), engine
					.getCommandRegistry(), engine.getMgr());
			newEngine.load(new InputStreamReader(in));
		} catch (Exception e) {
			throw new FileSystemException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
				;
			}
		}

	}

}
