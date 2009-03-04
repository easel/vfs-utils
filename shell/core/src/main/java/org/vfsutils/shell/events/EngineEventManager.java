package org.vfsutils.shell.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vfsutils.shell.Arguments;

public class EngineEventManager {

	private List eventListeners = new ArrayList();
    
    public synchronized void addEngineEventListener(EngineEventListener listener) {    	
    	eventListeners.add(listener);
    }
    
    public synchronized void removeEngineEventListener(EngineEventListener listener) {
    	eventListeners.remove(listener);
    }
    
    public synchronized void fireCommandStarted(Arguments args) {
    	Iterator iterator = eventListeners.iterator();
    	while (iterator.hasNext()) {
    		EngineEventListener listener = (EngineEventListener) iterator.next();
    		listener.commandStarted(args);
    	}
    }
    
    public synchronized void fireCommandFinished(Arguments args) {
    	Iterator iterator = eventListeners.iterator();
    	while (iterator.hasNext()) {
    		EngineEventListener listener = (EngineEventListener) iterator.next();
    		listener.commandFinished(args);
    	}
    }
    
    public synchronized void fireEngineStarted() {
    	Iterator iterator = eventListeners.iterator();
    	while (iterator.hasNext()) {
    		EngineEventListener listener = (EngineEventListener) iterator.next();
    		listener.engineStarted();
    	}
    }
    
    public synchronized void fireEngineStopping() {
    	Iterator iterator = eventListeners.iterator();
    	while (iterator.hasNext()) {
    		EngineEventListener listener = (EngineEventListener) iterator.next();
    		listener.engineStopping();
    	}
    }
    
    public synchronized void fireEngineStopped() {
    	Iterator iterator = eventListeners.iterator();
    	while (iterator.hasNext()) {
    		EngineEventListener listener = (EngineEventListener) iterator.next();
    		listener.engineStopped();
    	}
    }
}
