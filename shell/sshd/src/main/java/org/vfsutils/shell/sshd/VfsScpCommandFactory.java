package org.vfsutils.shell.sshd;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.command.UnknownCommand;
import org.vfsutils.factory.FileSystemManagerFactory;

/**
 * VfsScpCommand factory
 * @author kleij - at - users.sourceforge.net
 *
 */
public class VfsScpCommandFactory implements CommandFactory {

    private CommandFactory delegate;
    private FileSystemManagerFactory factory ;
    private String basePath;

    public VfsScpCommandFactory(FileSystemManagerFactory factory) {
    	this.factory = factory;
    	this.basePath = null;
    }
    
    public VfsScpCommandFactory(FileSystemManagerFactory factory, String basePath) {
    	this.factory = factory;
    	this.basePath = basePath;
    }

    public VfsScpCommandFactory(CommandFactory delegate, FileSystemManagerFactory factory, String basePath) {
        this.delegate = delegate;
        this.factory = factory;
    	this.basePath = basePath;
    }

    public Command createCommand(String command){
        String[] args = command.split(" ");
        if (args.length > 0 && "scp".equals(args[0])) {
            return new VfsScpCommand(factory, basePath, args);
        }
        if (delegate != null) {
            return delegate.createCommand(command);
        }
        return new UnknownCommand(command);
    }

}

