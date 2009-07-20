package org.vfsutils.shell.sshd;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

/**
 * VFS Shell SSHD Server based on Apache Mina SSHD
 * @author kleij - at - users.sourceforge.net
 *
 */
public class ShellServer {
	
	public static void main(String[] args) throws Exception {
        int port = 8000;
        String path = null;
        String root = null;
        boolean virtual = false;
        String domain = null;
        
        boolean error = false;

        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                if (i + 1 >= args.length) {
                    System.err.println("option requires an argument: " + args[i]);
                    break;
                }
                port = Integer.parseInt(args[++i]);
            } else if ("-path".equals(args[i])) {
                if (i + 1 >= args.length) {
                    System.err.println("option requires an argument: " + args[i]);
                    break;
                }
                path = args[++i];           
            } else if ("-domain".equals(args[i])) {
                if (i + 1 >= args.length) {
                    System.err.println("option requires an argument: " + args[i]);
                    break;
                }
                domain = args[++i];
            } else if ("--virtual".equals(args[i])) {
            	virtual = true;
            } else if (args[i].startsWith("-")) {
                System.err.println("illegal option: " + args[i]);
                error = true;
                break;
            } else if (root==null) { 
            	root = args[i];
        	} else {
                System.err.println("extra argument: " + args[i]);
                error = true;
                break;
            }
        }
        if (error) {
            System.err.println("usage: sshd [-p port] [-path path] [-domain domain] [--virtual] root");
            System.exit(-1);
        }
        
        System.out.println("Starting VFS Shell Server on port " + port + 
        		" and" + (virtual?" virtual ":" ") + "root " + root + 
        		(path==null?"":" (path is " + path +")") +
        		(domain==null?"":" (domain is " + domain + ")")
        		);
		
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		try {
			FileSystemManager fsManager = VFS.getManager();
			
			VfsShellFactory shellFactory = new VfsShellFactory(fsManager, path);
			sshd.setShellFactory(shellFactory);
			
			VfsPasswordAuthenticator pwdAuth = new VfsPasswordAuthenticator(fsManager, root, virtual);
			if (domain != null) {
				pwdAuth.setDomain(domain);
			}
			
			sshd.setPasswordAuthenticator(pwdAuth);
			sshd.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	

}
