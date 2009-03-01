package org.vfsutils.shell.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteShell extends Remote {
	
	public abstract Identity connect() throws RemoteException;
	
	public abstract ShellResponse handle(ShellRequest request, Identity identity) throws RemoteException;
	
	public abstract void disconnect(Identity identity) throws RemoteException;

}
