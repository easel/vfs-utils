package org.vfsutils.shell.sshd;

import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.vfsutils.factory.FileSystemManagerFactory;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandParser;
import org.vfsutils.shell.DefaultCommandParser;

/**
 * VFS Shell SSHD Server based on Apache Mina SSHD
 * @author kleij - at - users.sourceforge.net
 *
 */
public class Server {
	
	public static void main(String[] args) throws Exception {
        
        try {

        	int port;
        	String path;
        	String domain;
        	boolean virtual;
        	String root;
        	
        	//parse the arguments
        	try {
	        	CommandParser parser = new DefaultCommandParser();
	            Arguments arguments = parser.parse(args, false);
	        	arguments.assertSize(1);
	       		port = Integer.parseInt(arguments.getOption("port", "8000"));
	        	path = arguments.getOption("path");
	        	domain = arguments.getOption("domain");
	        	virtual = arguments.hasFlag("virtual");
	        	root = arguments.getArgument(0);
        	}
        	catch (IllegalArgumentException e) {
        		throw e;
        	}
        	catch (Exception e) {
        		throw new IllegalArgumentException(e);
        	}
        	
        	
            System.out.println("Starting VFS Shell Server on port " + port + 
            		" and" + (virtual?" virtual ":" ") + "root " + root + 
            		(path==null?"":" (path is " + path +")") +
            		(domain==null?"":" (domain is " + domain + ")")
            		);
    		
    		SshServer sshd = SshServer.setUpDefaultServer();
    		sshd.setPort(port);
    		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));

    		FileSystemManagerFactory factory = new FileSystemManagerFactory();
						
			VfsShellFactory shellFactory = new VfsShellFactory(factory, path);
			sshd.setShellFactory(shellFactory);
			
			VfsScpCommandFactory commandFactory = new VfsScpCommandFactory(factory);
			sshd.setCommandFactory(commandFactory);
			
			
			List<NamedFactory<Command>> subsystemFactories = new ArrayList<NamedFactory<Command>>();
			subsystemFactories.add(new VfsSftpSubsystem.Factory(factory, root)); 
			//subsystemFactories.add(new SftpSubsystem.Factory());
			sshd.setSubsystemFactories(subsystemFactories);
			
			VfsPasswordAuthenticator pwdAuth = new VfsPasswordAuthenticator(factory, root, virtual);
			if (domain != null) {
				pwdAuth.setDomain(domain);
			}
			
			sshd.setPasswordAuthenticator(pwdAuth);
			
			sshd.start();

        }
        catch (IllegalArgumentException e) {
        	System.err.println(e.getMessage());
        	System.err.println("usage: [--port=<port>] [--path=<path>] [--domain=<domain>] [--virtual] root");
            System.exit(-1);
        }
		catch (Exception e) {
			e.printStackTrace();
		}
	}	

}
