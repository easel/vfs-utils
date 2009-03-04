package org.vfsutils.shell.events;

import java.util.EventListener;

import org.vfsutils.shell.Arguments;

public interface EngineEventListener extends EventListener {
	
	public abstract void engineStarted();
	
	public abstract void engineStopping();
	
	public abstract void engineStopped();
	
	public abstract void waitingForCommand();
	
	public abstract void commandStarted(Arguments args);
	
	public abstract void commandFinished(Arguments args);

}
