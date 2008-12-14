package org.vfsutils.shell;

public class CommandException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7545050998384661027L;

	public CommandException() {
		super();
	}

	public CommandException(String message) {
		super(message);
	}

	public CommandException(Throwable cause) {
		super(cause);
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

}
