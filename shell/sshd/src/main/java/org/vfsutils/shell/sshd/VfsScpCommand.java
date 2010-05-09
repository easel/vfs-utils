package org.vfsutils.shell.sshd;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.factory.FileSystemManagerFactory;
import org.vfsutils.selector.FilenameSelector;

/**
 * Copy of the normal ScpCommand, but session aware and VFS enabled
 * 
 * @author kleij - at - users.sourceforge.net
 * 
 */
public class VfsScpCommand implements Command, Runnable, SessionAware {

	protected static final Logger log = LoggerFactory.getLogger(VfsScpCommand.class);
	protected static final int OK = 0;
    protected static final int WARNING = 1;
	protected static final int ERROR = 2;

    protected String name;
	protected boolean optR;
	protected boolean optT;
	protected boolean optF;
	protected boolean optV;
    protected boolean optD;
	protected boolean optP;

	// the root can be set via the session
	protected FileObject root;
	protected FileSystemManagerFactory factory;
	// the basepath is resolved within the root if the root has been set,
	// otherwise it serves as the root
	protected String basePath;
	
	// the path of the target file as communicated to the command
	protected String targetPath;
	protected InputStream in;
	protected OutputStream out;
	protected OutputStream err;
	protected ExitCallback callback;
	protected IOException error;

	public VfsScpCommand(FileSystemManagerFactory factory, String basePath, String[] args) {
		name = Arrays.asList(args).toString();
		if (log.isDebugEnabled()) {
			log.debug("Executing command {}", name);
		}
		this.targetPath = ".";		
		this.factory = factory;
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
                    case 'd':
                        optD = true;
                        break;
//                  default:
//                     error = new IOException("Unsupported option: " + args[i].charAt(j));
//                     return;
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

	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	public void start(Environment env) throws IOException {
		if (error != null) {
			throw error;
		}

		// set the target
		if (this.root == null && this.basePath == null) {
			throw new IOException("Not initialized with a valid path");
		}

		if (this.root == null) {
			FileSystemManager fsManager = this.factory.getManager();
			this.root = fsManager.resolveFile(this.basePath);
		} 

		new Thread(this, "VfsScpCommand: " + name).start();
	}
	
	public void destroy() {
	}

	public void run() {
		int exitValue = OK;
		String exitMessage = null;

		try {
            if (optT)
            {
				ack();
                for (; ;)
                {
                    String line;
                    boolean isDir = false;
                    int c = readAck(true);
                    switch (c)
                    {
                        case -1:
                            return;
                        case 'D':
                            isDir = true;
                        case 'C':
                        case 'E':
                            line = ((char) c) + readLine();
                            break;
                        default:
                            //a real ack that has been acted upon already
                            continue;
                    }

                    if (optR && isDir)
                    {
                        writeDir(line, resolveFile(targetPath));
                    }
                    else
                    {
                        writeFile(line, resolveFile(targetPath));
                    }
                }
			} else if (optF) {

				String pattern = targetPath;
                int idx = pattern.indexOf('*');
                if (idx >= 0) {
                	String basedir = "";
                    int lastSep = pattern.substring(0, idx).lastIndexOf('/');
                    if (lastSep >= 0) {
                        basedir = pattern.substring(0, lastSep);
                        pattern = pattern.substring(lastSep + 1);
                    }
                    
                	FilenameSelector selector = new FilenameSelector();
        			selector.setName(pattern);

        			List<FileObject> selected = new ArrayList<FileObject>();
        			//TODO: what if root not initialized
        			resolveFile(basedir).findFiles(selector, false, selected);

        			for (FileObject file : selected) {
                        if (file.getType().hasContent()) {
                            readFile(file);
                        } else if (file.getType().hasChildren()) {
                            if (!optR) {
                                out.write(WARNING);
                                out.write((file.getName().getBaseName() + " not a regular file\n").getBytes());
                            } else {
                                readDir(file);
                            }
                        } else {
                            out.write(WARNING);
                            out.write((file.getName().getBaseName() + " unknown file type\n").getBytes());
                        }
                    }
                } else {
                    String basedir = "";
                    int lastSep = pattern.lastIndexOf('/');
                    if (lastSep >= 0) {
                        basedir = pattern.substring(0, lastSep);
                        pattern = pattern.substring(lastSep + 1);
                    }
                    FileObject file = resolveFile(basedir).resolveFile(pattern);
				if (!file.exists()) {
                        throw new IOException(toString(file) + ": no such file or directory");
				}
				if (file.getType().equals(FileType.FILE)) {
					readFile(file);
				} else if (file.getType().equals(FileType.FOLDER)) {
					if (!optR) {
                            throw new IOException(toString(file) + " not a regular file");
					} else {
                            readDir(file);
					}
				} else {
                        throw new IOException(toString(file) + ": unknown file type");
}
				}
			} else {
				throw new IOException("Unsupported mode");
			}
		} catch (IOException e) {
			try {
				exitValue = ERROR;
				exitMessage = e.getMessage();
				out.write(exitValue);
				out.write(exitMessage.getBytes());
				out.write('\n');
				out.flush();
			} catch (IOException e2) {
				// Ignore
			}
            log.info("Error in scp command", e);
        } finally {
			if (callback != null) {
				callback.onExit(exitValue, exitMessage);
			}
		}
	}

	protected void writeDir(String header, FileObject path) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Writing dir {}", toString(path));
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
		} else if (!path.exists() && path.getParent().exists()
				&& path.getParent().getType().equals(FileType.FOLDER)) {
			file = path;
		} else {
			throw new IOException("Can not write to " + toString(path));
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
			log.debug("Writing file {}", toString(path));
		}
		if (!header.startsWith("C")) {
			throw new IOException("Expected a C message but got '" + header + "'");
		}

		String perms = header.substring(1, 5);
		int length = Integer.parseInt(header.substring(6, header.indexOf(' ', 6)));
		String name = header.substring(header.indexOf(' ', 6) + 1);

		FileObject file;
		if (path.exists() && path.getType().hasChildren()) {
			file = path.resolveFile(name);
		} else if (path.exists() && path.getType().hasContent()) {
			file = path;
		} else if (!path.exists() && path.getParent().exists()
				&& path.getParent().getType().hasChildren()) {
			file = path;
		} else {
			throw new IOException("Can not write to " + toString(path));
		}
		if (file.exists() && !file.getType().hasContent()) {
            throw new IOException("File is a directory: " + toString(file));
        } else if (file.exists() && !file.isWriteable()) {
            throw new IOException("Can not write to file: " + toString(file));
        }
		OutputStream os = file.getContent().getOutputStream();
		try {
			ack();

			byte[] buffer = new byte[8192];
			while (length > 0) {
				int len = (int) Math.min(length, buffer.length);
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
        readAck(false);
	}

    protected String readLine() throws IOException {
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

	protected void readFile(FileObject path) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Reading file {}", toString(path));
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
        readAck(false);

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
        readAck(false);
	}

	protected void readDir(FileObject path) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Reading directory {}", toString(path));
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
        readAck(false);

		for (FileObject child : path.getChildren()) {
			if (child.getType().equals(FileType.FILE)) {
				readFile(child);
			} else if (child.getType().equals(FileType.FOLDER)) {
				readDir(child);
			}
		}

		out.write("E\n".getBytes());
		out.flush();
        readAck(false);
	}

	protected void ack() throws IOException {
		out.write(0);
		out.flush();
	}

    protected int readAck(boolean canEof) throws IOException {
		int c = in.read();
		switch (c) {
            case -1:
                if (!canEof) {
                    throw new EOFException();
                }
			break;
            case OK:
                break;
            case WARNING:
			log.warn("Received warning: " + readLine());
			break;
            case ERROR:
			throw new IOException("Received nack: " + readLine());
            default:
                break;
		}
        return c;
	}

    protected FileObject resolveFile(String path) throws FileSystemException {
    	return this.root.resolveFile(path);
    }
    
    protected String toString(FileObject file) throws FileSystemException {
		if (this.root!=null) {
			return this.root.getName().getRelativeName(file.getName());
		}
		else {
			return file.getName().getFriendlyURI();
		}
	}

}
