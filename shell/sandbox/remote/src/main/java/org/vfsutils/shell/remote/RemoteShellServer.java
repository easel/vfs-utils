package org.vfsutils.shell.remote;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import org.vfsutils.shell.Engine;

public class RemoteShellServer extends UnicastRemoteObject
 implements RemoteShell {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8396622800436683487L;

	protected RemoteShellServer() throws RemoteException {
		super();
	}
	
	protected RemoteShellServer(int port) throws RemoteException {
		super(port);
	}

	private HashMap registry = new HashMap();
	
	public Identity connect() throws RemoteException {
		
		try {
			Identity id = new Identity();
			CommandRunner runner = new CommandRunner();
			Engine remoteEngine = new Engine(runner);
			runner.startEngine(remoteEngine);
			
			registry.put(id, runner);
			return id;
		}
		catch (IOException e) {
			throw new RemoteException("error on connect", e);
		}
	}

	public void disconnect(Identity identity) throws RemoteException {
		CommandRunner runner = (CommandRunner) registry.get(identity);
		runner.stopEngine();
		registry.remove(identity);

	}

	public ShellResponse handle(ShellRequest request, Identity identity)
			throws RemoteException {
		
		CommandRunner runner = (CommandRunner) registry.get(identity);
		try {
			return runner.handleInput(request);
		} catch (IOException e) {
			throw new RemoteException("Error in handling request", e);
		}		
	}
	
    public static void main ( String args[] ) throws Exception
    {
        // Assign a security manager, in the event that dynamic
	// classes are loaded
        if (System.getSecurityManager() == null)
            System.setSecurityManager ( new SecurityManager() );

        // Create an instance of our power service server ...
        RemoteShellServer svr = new RemoteShellServer(2021);

        // ... and bind it with the RMI Registry
        String name = "RemoteShell";
        /*RemoteShell stub =
            (RemoteShell) UnicastRemoteObject.exportObject(svr, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(name, stub);
*/
        Naming.bind (name, svr);


        System.out.println ("Service bound....");
    }


}
