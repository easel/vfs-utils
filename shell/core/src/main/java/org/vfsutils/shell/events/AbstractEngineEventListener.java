package org.vfsutils.shell.events;

import org.vfsutils.shell.Arguments;

public abstract class AbstractEngineEventListener implements
		EngineEventListener {

	public void commandFinished(Arguments args) {
	}

	public void commandStarted(Arguments args) {		
	}

	public void engineStarted() {
	}

	public void engineStopped() {
	}

	public void engineStopping() {
	}

	public void waitingForCommand() {
	}

}
