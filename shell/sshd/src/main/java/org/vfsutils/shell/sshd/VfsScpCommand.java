package org.vfsutils.shell.sshd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.CommandFactory.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfsScpCommand implements CommandFactory.Command, Runnable, SessionAware {
	
    private static final Logger log = LoggerFactory.getLogger(VfsScpCommand.class);

    private boolean optR;
    private boolean optT;
    private boolean optF;
    private boolean optV;
    private boolean optP;
    //the root can be set via the session
    private FileObject root;
    private FileSystemManager fsManager;
    //the basepath is resolved within the root if the root has been set
    //otherwise it serves as the root
    private String basePath;
    //the target file, it is resolved within the root using the targetPath
    private FileObject target;
    //the path of the target file as communicated to the command
    private String targetPath;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private CommandFactory.ExitCallback callback;
    private IOException error;

    public VfsScpCommand(FileSystemManager fsManager, String basePath, String[] args) {
        if (log.isDebugEnabled()) {
            log.debug("Executing command {}", Arrays.asList(args));
        }
        
        this.fsManager = fsManager;
        this.basePath = basePath;
        
        for (int i = 1; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                for (int j = 1; j < args[i].length(); j++) {
                    switch (args[i].charAt(j)) {
                        case 'f':
                            optF = true;
                            break;
                        case 'p':
                            optP = true;
                            break;
                        case 'r':
                            optR = true;
                            break;
                        case 't':
                            optT = true;
                            break;
                        case 'v':
                            optV = true;
                            break;
//                          default:
//                            error = new IOException("Unsupported option: " + args[i].charAt(j));
//                            return;
                    }
                }
            } else if (i == args.length - 1) {                
				targetPath = args[args.length - 1];
            }
            
        }
        if (!optF && !optT) {
            error = new IOException("Either -f or -t option should be set");
        }
    }

    public void setSession(ServerSession session) {
		FileObject storedRoot = session.getAttribute(VfsShellFactory.VFS_ROOT);
		if (storedRoot != null) {
			this.root = storedRoot;
		}
	}

	public void setInputStream(InputStream in) {
        this.in = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    public void setExitCallback(CommandFactory.ExitCallback callback) {
        this.callback = callback;
    }

    public void start() throws IOException {
        if (error != null) {
            throw error;
        }

        //set the target
        if (this.root==null && this.basePath==null) {
        	throw new IOException("Not initialized with a valid path");
        }
        
        if (this.root == null) {
        	this.target = this.fsManager.resolveFile(this.basePath).resolveFile(this.targetPath);
        }
        else if (this.basePath != null){
        	this.target = this.root.resolveFile(this.basePath).resolveFile(this.targetPath);
        }
        else {
        	this.target = this.root.resolveFile(this.targetPath);
        }

        new Thread(this).start();
    }

    public void run() {
        try {
            if (optT && !optR) {
                ack();
                writeFile(readLine(), target);
            } else if (optT && optR) {
                ack();
                writeDir(readLine(), target);
            } else if (optF) {
                if (!target.exists()) {
                    throw new IOException(target + ": no such file or directory");
                }
                if (target.getType().equals(FileType.FILE)) {
                    readFile(target);
                } else if (target.getType().equals(FileType.FOLDER)) {
                    if (!optR) {
                        throw new IOException(target + " not a regular file");
                    } else {
                        readDir(target);
                    }
                } else {
                    throw new IOException(target + ": unknown file type");
                }
            } else {
                throw new IOException("Unsupported mode");
            }
        } catch (IOException e) {
            try {
                out.write(2);
                out.write(e.getMessage().getBytes());
                out.write('\n');
                out.flush();
            } catch (IOException e2) {
                // Ignore
            }
            log.info("Error in scp command", e);
        } finally {
            callback.onExit(0);
        }
    }

    private void writeDir(String header, FileObject path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Writing dir {}", path);
        }
        if (!header.startsWith("D")) {
            throw new IOException("Expected a D message but got '" + header + "'");
        }

        String perms = header.substring(1, 5);
        int length = Integer.parseInt(header.substring(6, header.indexOf(' ', 6)));
        String name = header.substring(header.indexOf(' ', 6) + 1);

        if (length != 0) {
            throw new IOException("Expected 0 length for directory but got " + length);
        }
        FileObject file;
        if (path.exists() && path.getType().equals(FileType.FOLDER)) {
            file = path.resolveFile(name);
        } else if (!path.exists() && path.getParent().exists() && path.getParent().getType().equals(FileType.FOLDER)) {
            file = path;
        } else {
            throw new IOException("Can not write to " + path);
        }
        if (!(file.exists() && file.getType().equals(FileType.FOLDER))) {
       		file.createFolder();
        }

        ack();

        for (;;) {
            header = readLine();
            if (header.startsWith("C")) {
                writeFile(header, file);
            } else if (header.startsWith("D")) {
                writeDir(header, file);
            } else if (header.equals("E")) {
                ack();
                break;
            } else {
                throw new IOException("Unexpected message: '" + header + "'");
            }
        }

    }

    private void writeFile(String header, FileObject path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Writing file {}", path);
        }
        if (!header.startsWith("C")) {
            throw new IOException("Expected a C message but got '" + header + "'");
        }

        String perms = header.substring(1, 5);
        int length = Integer.parseInt(header.substring(6, header.indexOf(' ', 6)));
        String name = header.substring(header.indexOf(' ', 6) + 1);

        FileObject file;
        if (path.exists() && path.getType().equals(FileType.FOLDER)) {
            file = path.resolveFile(name);
        } else if (path.exists() && path.getType().equals(FileType.FILE)) {
            file = path;
        } else if (!path.exists() && path.getParent().exists() && path.getParent().getType().equals(FileType.FOLDER)) {
            file = path;
        } else {
            throw new IOException("Can not write to " + path);
        }
        OutputStream os = file.getContent().getOutputStream();
        try {
            ack();

            byte[] buffer = new byte[8192];
            while (length > 0) {
                int len = Math.min(length, buffer.length);
                len = in.read(buffer, 0, len);
                if (len <= 0) {
                    throw new IOException("End of stream reached");
                }
                os.write(buffer, 0, len);
                length -= len;
            }
        } finally {
            os.close();
        }

        ack();
        readAck();
    }

    private String readLine() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (;;) {
            int c = in.read();
            if (c == '\n') {
                return baos.toString();
            } else if (c == -1) {
                throw new IOException("End of stream");
            } else {
                baos.write(c);
            }
        }
    }

    private void readFile(FileObject path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Reading file {}", path);
        }
        StringBuffer buf = new StringBuffer();
        buf.append("C");
        buf.append("0644"); // what about perms
        buf.append(" ");
        buf.append(path.getContent().getSize()); // length
        buf.append(" ");
        buf.append(path.getName().getBaseName());
        buf.append("\n");
        out.write(buf.toString().getBytes());
        out.flush();
        readAck();

        InputStream is = path.getContent().getInputStream();
        try {
            byte[] buffer = new byte[8192];
            for (;;) {
                int len = is.read(buffer, 0, buffer.length);
                if (len == -1) {
                    break;
                }
                out.write(buffer, 0, len);
            }
        } finally {
            is.close();
        }
        ack();
        readAck();
    }

    private void readDir(FileObject path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Reading directory {}", path);
        }
        StringBuffer buf = new StringBuffer();
        buf.append("D");
        buf.append("0755"); // what about perms
        buf.append(" ");
        buf.append("0"); // length
        buf.append(" ");
        buf.append(path.getName().getBaseName());
        buf.append("\n");
        out.write(buf.toString().getBytes());
        out.flush();
        readAck();

        for (FileObject child : path.getChildren()) {
            if (child.getType().equals(FileType.FILE)) {
                readFile(child);
            } else if (child.getType().equals(FileType.FOLDER)) {
                readDir(child);
            }
        }

        out.write("E\n".getBytes());
        out.flush();
        readAck();
    }

    private void ack() throws IOException {
        out.write(0);
        out.flush();
    }

    private void readAck() throws IOException {
        int c = in.read();
        switch (c) {
            case 0:
                break;
            case 1:
                log.warn("Received warning: " + readLine());
                break;
            case 2:
                throw new IOException("Received nack: " + readLine());
        }
    }


}
