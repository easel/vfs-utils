package org.vfsutils.shell.commands;

import org.apache.commons.vfs.FileSystemException;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public class Error extends AbstractCommand {

	public Error() {
		super(
				"error",
				new CommandInfo("Info on last error",
						"[-p|--halt|--halt=<yes/no>|--assert|--assert=<message>|--clear]"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

		if (args.hasFlag("halt")) {
			showHaltOnError(engine);
		} else if (args.hasOption("halt")) {
			String value = args.getOption("halt");
			boolean halt = (value.equalsIgnoreCase("yes")
					|| value.equalsIgnoreCase("y")
					|| value.equalsIgnoreCase("true") || value
					.equalsIgnoreCase("t"));

			setHaltOnError(halt, engine);
		} else if (args.hasOption("assert")) {
			assertError(args.getOption("assert"), engine);
		} else if (args.hasFlag("assert")) {
			assertError(null, engine);
		} else if (args.hasFlag("clear")) {
			clearError(engine);
		} else if (args.hasFlag('p')) {
			printError(engine);
		} else {
			lastError(engine);
		}
	}

	public void lastError(Engine engine) {
		if (engine.getLastError() != null) {
			engine.println(engine.getLastError().getMessage());
		} else {
			engine.println("no error");
		}
	}

	public void printError(Engine engine) {
		if (engine.getLastError() != null) {
			engine.getLastError().printStackTrace(engine.getConsole().getOut());
		} else {
			engine.println("no error");
		}
	}

	public void setHaltOnError(boolean haltOnError, Engine engine) {
		engine.setHaltOnError(haltOnError);
		showHaltOnError(engine);
	}

	public void showHaltOnError(Engine engine) {
		engine.println("Halting on error : " + engine.isHaltOnError());
	}

	/**
	 * Throws an exception when there is no error
	 * 
	 * @param message
	 *            the message to show
	 * @param engine
	 * @throws CommandException
	 */
	public void assertError(String message, Engine engine)
			throws CommandException {
		if (engine.getLastError() == null) {
			if (message == null || message.length() == 0) {
				message = "No error found!";
			}
			throw new CommandException(message);
		}
	}

	/**
	 * Clears the last error on the engine
	 * 
	 * @param engine
	 */
	public void clearError(Engine engine) {
		engine.clearLastError();
	}
}
