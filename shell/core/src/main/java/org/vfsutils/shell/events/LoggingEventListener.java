package org.vfsutils.shell.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.Engine;

public class LoggingEventListener implements EngineEventListener {

	public static final int NONE = 0;
	public static final int TRACE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARN = 4;
	public static final int ERROR = 5;
	public static final int FATAL = 6;

	protected Log log = LogFactory.getLog(LoggingEventListener.class);
	protected int level;
	protected int errorLevel;

	protected Engine engine;

	public LoggingEventListener(Engine engine) {
		this(DEBUG, DEBUG, engine);
	}

	public LoggingEventListener(int level, Engine engine) {
		this(level, level, engine);
	}

	public LoggingEventListener(int level, int errorLevel, Engine engine) {
		this.level = level;
		this.errorLevel = errorLevel;
		this.engine = engine;
	}

	public int getLevel() {
		return this.level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getErrorLevel() {
		return this.errorLevel;
	}

	public void setErrorLevel(int errorLevel) {
		this.errorLevel = errorLevel;
	}

	public void commandFailed(Arguments args, Exception e) {
		error("Command [" + args.getCmd() + "] failed", e);
	}

	public void commandFinished(Arguments args) {
		log("Command [" + args.getCmd() + "] finished");
	}

	public void commandStarted(Arguments args) {
		log("Command ["
				+ engine.getCommandParser().toString(args.getAllTokens())
				+ "] started");
	}

	public void engineStarted() {
		log("Engine started");
	}

	public void engineStopped() {
		log("Engine stopped");
	}

	public void engineStopping() {
		log("Engine stopping");
	}

	public void waitingForCommand() {
		log("Waiting for command...");
	}

	protected void log(Object message) {
		switch (level) {
		case TRACE:
			log.trace(message);
			break;
		case DEBUG:
			log.debug(message);
			break;
		case INFO:
			log.info(message);
			break;
		case WARN:
			log.warn(message);
			break;
		case ERROR:
			log.error(message);
			break;
		case FATAL:
			log.fatal(message);
			break;
		default:
			break;
		}
	}

	protected void error(Object message, Exception e) {
		switch (errorLevel) {
		case TRACE:
			log.trace(message, e);
			break;
		case DEBUG:
			log.debug(message, e);
			break;
		case INFO:
			log.info(message, e);
			break;
		case WARN:
			log.warn(message, e);
			break;
		case ERROR:
			log.error(message, e);
			break;
		case FATAL:
			log.fatal(message, e);
			break;
		default:
			break;
		}
	}

}
