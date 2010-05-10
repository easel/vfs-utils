package org.vfsutils.shell.sshd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileNotFoundException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.common.util.SelectorUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vfsutils.content.PseudoRandomAccessContent;
import org.vfsutils.content.PseudoWriteableRandomAccessContent;
import org.vfsutils.factory.FileSystemManagerFactory;

/**
 * SFTP subsystem
 * Copy of org.apache.sshd.server.sftp.SftpSubsystem and adapted for VFS where needed.
 * @see org.apache.sshd.server.sftp.SftpSubsystem 
 * @author kleij - at - users.sourceforge.net
 */
public class VfsSftpSubsystem extends org.apache.sshd.server.sftp.SftpSubsystem implements Command, Runnable, SessionAware {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
    
	public static class Factory implements NamedFactory<Command> {
        
		private FileSystemManagerFactory factory;
		private String rootPath;
		
		public Factory(FileSystemManagerFactory factory, String rootPath) {
			this.factory = factory;
			this.rootPath = rootPath;
		}
		
		public Command create() {		
            return new VfsSftpSubsystem(factory, this.rootPath);
        }
		
        public String getName() {
            return "sftp";
        }
    }

    private ExitCallback callback;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private Environment env;
    private ServerSession session;
    private boolean closed = false;

    // the factory and rootpath are set via the constructor
    private FileSystemManagerFactory factory;
    private String rootPath;
    // the root can be overridden via the session
	private FileObject root;
	

    private int version;
    private Map<String, Handle> handles = new HashMap<String, Handle>();
    
    protected static abstract class Handle {
        FileObject file;

        public Handle(FileObject file) {
            this.file = file;
        }

        public FileObject getFile() {
            return file;
        }

        public void close() throws IOException {
        }

    }

    protected static class DirectoryHandle extends Handle {
        boolean done;

        public DirectoryHandle(FileObject file) {
            super(file);
        }
        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }
    }

    protected static class FileHandle extends Handle {
    	private int flags;
        private RandomAccessContent raf = null;

        public FileHandle(FileObject file, RandomAccessContent raf, int flags) {
            super(file);
            this.raf = raf;
            this.flags = flags;
        }

        public RandomAccessContent getRaf() throws FileSystemException {
        	return this.raf;
        }

        public int getFlags() {
            return flags;
        }

        @Override
        public void close() throws IOException {
        	raf.close();
        }
    }

    public VfsSftpSubsystem(FileSystemManagerFactory factory, String rootPath) {
    	// need to call super with a dummy value
    	super(new File("."));
    	this.factory = factory;
    	this.rootPath = rootPath;
    }
    
	public void setSession(ServerSession session) {
		this.session = session;
		
		FileObject storedRoot = session.getAttribute(VfsShellFactory.VFS_ROOT);
		if (storedRoot != null) {
			this.root = storedRoot;
		}
	}

    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
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

    public void start(Environment env) throws IOException {
        this.env = env;
        
        if (this.root==null){
        	FileSystemManager fsManager = this.factory.getManager();
        	this.root = fsManager.resolveFile(this.rootPath);
        }
        
        new Thread(this).start();
    }

    public void run() {
		DataInputStream dis = null;
        try {
            dis = new DataInputStream(in);
            while (true) {
                int  length = dis.readInt();
                if (length < 5) {
                    throw new IllegalArgumentException();
                }
                Buffer buffer = new Buffer(length + 4);
                buffer.putInt(length);
                int nb = length;
                while (nb > 0) {
                    int l = dis.read(buffer.array(), buffer.wpos(), nb);
                    if (l < 0) {
                        throw new IllegalArgumentException();
                    }
                    buffer.wpos(buffer.wpos() + l);
                    nb -= l;
                }
                process(buffer);
            }
        } catch (Throwable t) {
            if (!closed) {
                log.error("Exception caught in SFTP subsystem", t);
            }
        } finally {
		    if (dis != null) {
        		try {
        			dis.close();
        		} catch (IOException ioe) {
        			log.error("Could not close DataInputStream", ioe);
        		}
        	}
        	dis = null;

            callback.onExit(0);
        }
    }

    protected void process(Buffer buffer) throws IOException {
        int length = buffer.getInt();
        int type   = buffer.getByte();
        int id     = buffer.getInt();
        switch (type) {
            case SSH_FXP_INIT: {
                if (length != 5) {
                    throw new IllegalArgumentException();
                }
                version = id;
                if (version >= LOWER_SFTP_IMPL && version <= HIGHER_SFTP_IMPL) {
	                buffer.clear();
	                buffer.putByte((byte) SSH_FXP_VERSION);
                    buffer.putInt(version);
	                send(buffer);
                } else {
                	// We only support version 3 (Version 1 and 2 are not common)
                    sendStatus(id, SSH_FX_OP_UNSUPPORTED, "SFTP server only support versions " + ALL_SFTP_IMPL);
                }

                break;
            }
            case SSH_FXP_OPEN: {
                if (version <= 4) {
                    String path   = buffer.getString();
                    int    pflags = buffer.getInt();
                    // attrs
                    try {
                        FileObject file = this.resolveFile(path);
                        
                        if (file.exists()) {
                            if (((pflags & SSH_FXF_CREAT) != 0) && ((pflags & SSH_FXF_EXCL) != 0)) {
                                sendStatus(id, SSH_FX_FILE_ALREADY_EXISTS, path);
                                return;
                            }
                        } else {
                            if (((pflags & SSH_FXF_CREAT) != 0)) {
                            	try {
                            		file.createFile();
                            	} catch (FileSystemException e) {
                                    sendStatus(id, SSH_FX_FAILURE, "Can not create " + path);
                                }
                            }
                        }
                        RandomAccessMode ram = ((pflags & SSH_FXF_WRITE) != 0 ? RandomAccessMode.READWRITE : RandomAccessMode.READ);
                        
                        //TODO: do not know what to do with this
//                        if ((pflags & SSH_FXF_TRUNC) != 0) {
//                            raf.setLength(0);
//                        }
                        String handle = UUID.randomUUID().toString();
                        handles.put(handle, new FileHandle(file, getRaf(file, ram), pflags)); // handle flags conversion
                        sendHandle(id, handle);
                    } catch (IOException e) {
                        sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                    }
                } else {
                    String path  = buffer.getString();
                    int    acc   = buffer.getInt();
                    int    flags = buffer.getInt();
                    // attrs
                    try {
                        FileObject file = this.resolveFile(path);
                        RandomAccessMode ram;
                        switch (flags & SSH_FXF_ACCESS_DISPOSITION) {
                            case SSH_FXF_CREATE_NEW: {
                                if (file.exists()) {
                                    sendStatus(id, SSH_FX_FILE_ALREADY_EXISTS, path);
                                    return;
                                } else {
                                	try {
                                		file.createFile();
                                	} catch (FileSystemException e){
                                		sendStatus(id, SSH_FX_FAILURE, "Can not create " + path);
                                	}                                    
                                }
                                ram = RandomAccessMode.READWRITE; // TODO: handle access
                                break;
                            }
                            case SSH_FXF_CREATE_TRUNCATE: {
                                if (file.exists()) {
                                    sendStatus(id, SSH_FX_FILE_ALREADY_EXISTS, path);
                                    return;
                                } else {
                                	try {
                                		file.createFile();
                                	} catch (FileSystemException e){
                                		sendStatus(id, SSH_FX_FAILURE, "Can not create " + path);
                                	} 
                                }
                                ram = RandomAccessMode.READWRITE;; // TODO: handle access
                                //TODO: what to do with this?
                                //raf.setLength(0);
                                break;
                            }
                            case SSH_FXF_OPEN_EXISTING: {
                                if (!file.exists()) {
                                    if (!file.getParent().exists()) {
                                        sendStatus(id, SSH_FX_NO_SUCH_PATH, path);
                                    } else {
                                        sendStatus(id, SSH_FX_NO_SUCH_FILE, path);
                                    }
                                    return;
                                }
                                ram = RandomAccessMode.READWRITE; // TODO: handle access
                                break;
                            }
                            case SSH_FXF_OPEN_OR_CREATE: {
                            	ram = RandomAccessMode.READWRITE; // TODO: handle access
                                break;
                            }
                            case SSH_FXF_TRUNCATE_EXISTING: {
                                if (!file.exists()) {
                                    if (!file.getParent().exists()) {
                                        sendStatus(id, SSH_FX_NO_SUCH_PATH, path);
                                    } else {
                                        sendStatus(id, SSH_FX_NO_SUCH_FILE, path);
                                    }
                                    return;
                                }
                                ram = RandomAccessMode.READWRITE; // TODO: handle access
                                //TODO: what to do with this
                                //raf.setLength(0);
                                break;
                            }
                            default:
                                throw new IllegalArgumentException("Unsupported open mode: " + flags);
                        }
                        String handle = UUID.randomUUID().toString();
                        handles.put(handle, new FileHandle(file, getRaf(file, ram), flags));
                        sendHandle(id, handle);
                    } catch (IOException e) {
                        sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                    }
                }
                break;
            }
            case SSH_FXP_CLOSE: {
                String handle = buffer.getString();
                try {
                    Handle h = handles.get(handle);
                    if (h == null) {
                        sendStatus(id, SSH_FX_INVALID_HANDLE, handle, "");
                    } else {
                        handles.remove(handle);
                        h.close();
                        sendStatus(id, SSH_FX_OK, "", "");
                    }
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_READ: {
                String handle = buffer.getString();
                long   offset = buffer.getLong();
                int    len    = buffer.getInt();
                try {
                    Handle p = handles.get(handle);
                    if (!(p instanceof FileHandle)) {
                        sendStatus(id, SSH_FX_INVALID_HANDLE, handle);
                    } else {
                        RandomAccessContent raf = ((FileHandle) p).getRaf();
                        raf.seek(offset);
                        //TODO: ok?
                        InputStream ras = raf.getInputStream();
                        byte[] b = new byte[len];
                        len = ras.read(b);
                        if (len >= 0) {
                            Buffer buf = new Buffer(len + 5);
                            buf.putByte((byte) SSH_FXP_DATA);
                            buf.putInt(id);
                            buf.putBytes(b, 0, len);
                            buf.putBoolean(len == 0);
                            send(buf);
                        } else {
                            sendStatus(id, SSH_FX_EOF, "");
                        }
                    }
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_WRITE: {
                String handle = buffer.getString();
                long   offset = buffer.getLong();
                byte[] data   = buffer.getBytes();
                try {
                    Handle p = handles.get(handle);
                    if (!(p instanceof FileHandle)) {
                        sendStatus(id, SSH_FX_INVALID_HANDLE, handle);
                    } else {
                        RandomAccessContent raf = ((FileHandle) p).getRaf();
                        raf.seek(offset); // TODO: handle append flags
                        raf.write(data);
                        sendStatus(id, SSH_FX_OK, "");
                    }
                } catch (UnsupportedOperationException e) {
                    sendStatus(id, SSH_FX_FAILURE, "Not supported");
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_LSTAT:
            case SSH_FXP_STAT: {
                String path = buffer.getString();
                try {
                    FileObject p = this.resolveFile(path);
                    sendAttrs(id, p);
                } catch (FileNotFoundException e) {
                    sendStatus(id, SSH_FX_NO_SUCH_FILE, e.getMessage());
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_FSTAT: {
                String handle = buffer.getString();
                try {
                    Handle p = handles.get(handle);
                    if (p == null) {
                        sendStatus(id, SSH_FX_INVALID_HANDLE, handle);
                    } else {
                        sendAttrs(id, p.getFile());
                    }
                } catch (FileNotFoundException e) {
                    sendStatus(id, SSH_FX_NO_SUCH_FILE, e.getMessage());
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_OPENDIR: {
                String path = buffer.getString();
                try {
                    FileObject p = this.resolveFile(path);
                    if (!p.exists()) {
                        sendStatus(id, SSH_FX_NO_SUCH_FILE, path);
                    } else if (!p.getType().hasChildren()) {
                        sendStatus(id, SSH_FX_NOT_A_DIRECTORY, path);
                    } else  if (!p.isReadable()) {
                        sendStatus(id, SSH_FX_PERMISSION_DENIED, path);
                    } else {
                        String handle = UUID.randomUUID().toString();
                        handles.put(handle, new DirectoryHandle(p));
                        sendHandle(id, handle);
                    }
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_READDIR: {
                String handle = buffer.getString();
                try {
                    Handle p = handles.get(handle);
                    if (!(p instanceof DirectoryHandle)) {
                        sendStatus(id, SSH_FX_INVALID_HANDLE, handle);
                    } else if (((DirectoryHandle) p).isDone()) {
                        sendStatus(id, SSH_FX_EOF, "", "");
                    } else if (!p.getFile().exists()) {
                        sendStatus(id, SSH_FX_NO_SUCH_FILE, p.getFile().getName().getPath());
                    } else if (!p.getFile().getType().hasChildren()) {
                        sendStatus(id, SSH_FX_NOT_A_DIRECTORY, p.getFile().getName().getPath());
                    } else if (!p.getFile().isReadable()) {
                        sendStatus(id, SSH_FX_PERMISSION_DENIED, p.getFile().getName().getPath());
                    } else {
                        sendName(id, p.getFile().getChildren());
                        ((DirectoryHandle) p).setDone(true);
                    }
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_REMOVE: {
                String path = buffer.getString();
                try {
                    FileObject p = this.resolveFile(path);
                    if (!p.exists()) {
                        sendStatus(id, SSH_FX_NO_SUCH_FILE, p.getName().getPath());
                    } else if (p.getType().hasChildren()) {
                        sendStatus(id, SSH_FX_FILE_IS_A_DIRECTORY, p.getName().getPath());
                    } else if (!p.delete()) {
						sendStatus(id, SSH_FX_FAILURE, "Failed to delete file");
                    } else {
						sendStatus(id, SSH_FX_OK, "");
					}
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_MKDIR: {
                String path = buffer.getString();
                // attrs
                try {
                    FileObject p = this.resolveFile(path);
                    if (p.exists()) {
                        if (p.getType().hasChildren()) {
                            sendStatus(id, SSH_FX_FILE_ALREADY_EXISTS, p.getName().getPath());
                        } else {
                            sendStatus(id, SSH_FX_NOT_A_DIRECTORY, p.getName().getPath());
                        }
                    } else {
                    	try {
                    		p.createFolder();
                            sendStatus(id, SSH_FX_OK, "");
                    	} catch (FileSystemException e){
                    		throw new IOException("Error creating dir " + path);
                    	}            
                    }
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_RMDIR: {
                String path = buffer.getString();
                // attrs
                try {
                    FileObject p = this.resolveFile(path);
                    if (p.getType().hasChildren()) {
                        if (p.exists()) {
                            if (p.getChildren().length == 0) {
                                if (p.delete()) {
                                    sendStatus(id, SSH_FX_OK, "");
                                } else {
                                    sendStatus(id, SSH_FX_FAILURE, "Unable to delete directory " + path);
                                }
                            } else {
                                sendStatus(id, SSH_FX_DIR_NOT_EMPTY, path);
                            }
                        } else {
                            sendStatus(id, SSH_FX_NO_SUCH_PATH, path);
                        }
                    } else {
                        sendStatus(id, SSH_FX_NOT_A_DIRECTORY, p.getName().getPath());
                    }
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_REALPATH: {
                String path = buffer.getString();
                if (path.trim().length() == 0) {
                    path = ".";
                }
                // TODO: handle optional args
                try {
                    log.info("path="+path);
                    FileObject p = resolveFile(path);
                    sendPath(id, path, p);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    sendStatus(id, SSH_FX_NO_SUCH_FILE, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
                break;
            }
            case SSH_FXP_RENAME: {
            	String oldPath = buffer.getString();
            	String newPath = buffer.getString();
                try {
                    FileObject o = this.resolveFile(oldPath);
                    FileObject n = this.resolveFile(newPath);
                    if (!o.exists()) {
                        sendStatus(id, SSH_FX_NO_SUCH_FILE, o.getName().getPath());
                    } else if (n.exists()) {
                        sendStatus(id, SSH_FX_FILE_ALREADY_EXISTS, n.getName().getPath());
                    } else if (!o.canRenameTo(n)){
						sendStatus(id, SSH_FX_FAILURE, "Cannot rename file");
                    } else {
                    	try {
                    		o.moveTo(n);
                    		sendStatus(id, SSH_FX_OK, "");
                    	}
                    	catch (FileSystemException e) {
                    		sendStatus(id, SSH_FX_FAILURE, "Cannot rename file");
                    	}
					}
                } catch (IOException e) {
                    sendStatus(id, SSH_FX_FAILURE, e.getMessage());
                }
            	break;
            }
			case SSH_FXP_SETSTAT:
            case SSH_FXP_FSETSTAT: {
            	// This is required for WinSCP / Cyberduck to upload properly
            	// Blindly reply "OK"
				// TODO implement it
                sendStatus(id, SSH_FX_OK, "");
            	break;
            }   
			
            default:
                log.error("Received: {}", type);
                sendStatus(id, SSH_FX_OP_UNSUPPORTED, "Command " + type + " is unsupported or not implemented");
                throw new IllegalStateException();
        }
    }

    protected void sendHandle(int id, String handle) throws IOException {
        Buffer buffer = new Buffer();
        buffer.putByte((byte) SSH_FXP_HANDLE);
        buffer.putInt(id);
        buffer.putString(handle);
        send(buffer);
    }

    protected void sendAttrs(int id, FileObject file) throws IOException {
        Buffer buffer = new Buffer();
        buffer.putByte((byte) SSH_FXP_ATTRS);
        buffer.putInt(id);
        writeAttrs(buffer, file);
        send(buffer);
    }
	
    protected void sendAttrs(int id, FileObject file, int flags) throws IOException {
        Buffer buffer = new Buffer();
        buffer.putByte((byte) SSH_FXP_ATTRS);
        buffer.putInt(id);
        writeAttrs(buffer, file, flags);
        send(buffer);
    }

    protected void sendPath(int id, String path, FileObject f) throws IOException {
        Buffer buffer = new Buffer();
        buffer.putByte((byte) SSH_FXP_NAME);
        buffer.putInt(id);
        buffer.putInt(1);
        //normalize the given path, use *nix style separator
        String normalizedPath = SelectorUtils.normalizePath(path, "/");
        if (normalizedPath.length()==0) {
        	normalizedPath = "/";
        }
        buffer.putString(normalizedPath);
        if (version <= 3) {
            buffer.putString(getLongName(f)); // Format specified in the specs
            buffer.putInt(0);
        } else {
            buffer.putString(getName(f)); // Supposed to be UTF-8
            writeAttrs(buffer, f);
        }
        send(buffer);
    }


    protected void sendName(int id, FileObject... files) throws IOException {
        Buffer buffer = new Buffer();
        buffer.putByte((byte) SSH_FXP_NAME);
        buffer.putInt(id);
        buffer.putInt(files.length);
        for (FileObject f : files) {
        	//check now, otherwise strange exceptions occur
        	if (!f.exists()) {
        		throw new FileNotFoundException(getName(f));
        	}
			buffer.putString(getName(f));
            if (version <= 3) {
                buffer.putString(getLongName(f)); // Format specified in the specs
            } else {
				buffer.putString(getName(f)); // Supposed to be UTF-8
			}
            writeAttrs(buffer, f);
        }
        send(buffer);
    }    
    
    
    
    private String getLongName(FileObject f) throws FileSystemException {
    	String username = session.getUsername();
    	if (username.length() > 8) {
    		username = username.substring(0, 8);
    	} else {
    		for (int i=username.length(); i < 8; i++) {
    			username = username + " ";
    		}
    	}
    	
    	// VFS will throw an exception when getting size on folders
    	long length =  (f.getType().hasContent() ? f.getContent().getSize() : 0);
    	long modifiedTime = (f.exists()? f.getContent().getLastModifiedTime() : 0);
    	
    	String lengthString = String.format("%1$#8s", length);
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append((f.getType().hasChildren() ? "d" : "-")); 
    	sb.append((f.isReadable() ? "r" : "-")); 
    	sb.append((f.isWriteable() ? "w" : "-")); 
    	sb.append((/*f.canExecute() ? "x" :*/ "-")); 
    	sb.append((false ? "x" : "-"));
    	sb.append((f.isReadable() ? "r" : "-")); 
    	sb.append((f.isWriteable() ? "w" : "-")); 
    	sb.append((/*f.canExecute() ? "x" :*/ "-")); 
    	sb.append((false ? "x" : "-"));
    	sb.append((f.isReadable() ? "r" : "-")); 
    	sb.append((f.isWriteable() ? "w" : "-")); 
    	sb.append((/*f.canExecute() ? "x" :*/ "-"));
    	sb.append((false ? "x" : "-"));
    	sb.append(" ");
    	sb.append("  1");
    	sb.append(" ");
    	sb.append(username);
    	sb.append(" ");
    	sb.append(username);
    	sb.append(" ");
    	sb.append(lengthString);
    	sb.append(" ");
    	sb.append(getUnixDate(modifiedTime));
    	sb.append(" ");
    	sb.append(getName(f));
    	
    	return sb.toString();
    }
    
    protected void writeAttrs(Buffer buffer, FileObject file) throws IOException {
    	writeAttrs(buffer, file, 0);
    }


    protected void writeAttrs(Buffer buffer, FileObject file, int flags) throws IOException {
        
    	long size = ( file.getType().hasContent() ? file.getContent().getSize() : 0);
    	long modifiedTime = (file.exists() ? file.getContent().getLastModifiedTime() : 0);
    	
        if (version >= 4) {
        	String username = session.getUsername();
        	long lastModif = file.getContent().getLastModifiedTime();
            int p = 0;
            if (file.isReadable()) {
                p |= S_IRUSR;
            }
            if (file.isWriteable()) {
                p |= S_IWUSR;
            }
            /*
            if (file.canExecute()) {
                p |= S_IXUSR;
            }
            */
            if (file.getType().hasContent()) {
                buffer.putInt(SSH_FILEXFER_ATTR_PERMISSIONS);
                buffer.putByte((byte) SSH_FILEXFER_TYPE_REGULAR);
                buffer.putInt(p);
            } else if (file.getType().hasChildren()) {
                buffer.putInt(SSH_FILEXFER_ATTR_PERMISSIONS);
                buffer.putByte((byte) SSH_FILEXFER_TYPE_DIRECTORY);
                buffer.putInt(p);
            } else {
                buffer.putInt(0);
                buffer.putByte((byte) SSH_FILEXFER_TYPE_UNKNOWN);
            }
        } else {
            int p = 0;
            if (file.getType().hasContent()) {
                p |= 0100000;
            }
            if (file.getType().hasChildren()) {
                p |= 0040000;
            }
            if (file.isReadable()) {
                p |= 0000400;
            }
            if (file.isWriteable()) {
                p |= 0000200;
            }
            /*
            if (file.canExecute()) {
            	p |= 0000100;
            }
            */
            if (file.getType().hasContent()) {
            	buffer.putInt(SSH_FILEXFER_ATTR_SIZE | SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME);
            	buffer.putLong(size);            	
                buffer.putInt(p);
                buffer.putInt(modifiedTime/1000);
                buffer.putInt(modifiedTime/1000);
            } else if (file.getType().hasChildren()) {
            	buffer.putInt(SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME);
                buffer.putInt(p);
                buffer.putInt(modifiedTime/1000);
                buffer.putInt(modifiedTime/1000);
            } else {
                buffer.putInt(0);
            }
        }
    }

    protected void sendStatus(int id, int substatus, String msg) throws IOException {
        sendStatus(id, substatus, msg, "");
    }

    protected void sendStatus(int id, int substatus, String msg, String lang) throws IOException {
        Buffer buffer = new Buffer();
        buffer.putByte((byte) SSH_FXP_STATUS);
        buffer.putInt(id);
        buffer.putInt(substatus);
        buffer.putString(msg);
        buffer.putString(lang);
        send(buffer);
    }

    protected void send(Buffer buffer) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(buffer.available());
        dos.write(buffer.array(), buffer.rpos(), buffer.available());
        dos.flush();
    }

    public void destroy() {
        closed = true;
    }

    private FileObject resolveFile(String path) throws FileSystemException {
    	return this.root.resolveFile(path);
    }
    
    private final static String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    
    /**
     * Get unix style date string.
     */
    private final static String getUnixDate(long millis) {
        if (millis < 0) {
            return "------------";
        }

        StringBuffer sb = new StringBuffer(16);
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);

        // month
        sb.append(MONTHS[cal.get(Calendar.MONTH)]);
        sb.append(' ');

        // day
        int day = cal.get(Calendar.DATE);
        if (day < 10) {
            sb.append(' ');
        }
        sb.append(day);
        sb.append(' ');

        long sixMonth = 15811200000L; // 183L * 24L * 60L * 60L * 1000L;
        long nowTime = System.currentTimeMillis();
        if (Math.abs(nowTime - millis) > sixMonth) {

            // year
            int year = cal.get(Calendar.YEAR);
            sb.append(' ');
            sb.append(year);
        } else {

            // hour
            int hh = cal.get(Calendar.HOUR_OF_DAY);
            if (hh < 10) {
                sb.append('0');
            }
            sb.append(hh);
            sb.append(':');

            // minute
            int mm = cal.get(Calendar.MINUTE);
            if (mm < 10) {
                sb.append('0');
            }
            sb.append(mm);
        }
        return sb.toString();
    }

    
    
    private String getPath(FileObject f) throws FileSystemException {
    	String path = this.root.getName().getRelativeName(f.getName());
    	if (path.equals(".")) {
    		path="/";
    	}
    	else {
    		path = "/" + path;
    	}
    	return path;
    }

    private String getName(FileObject f) {
    	String name = f.getName().getBaseName();
    	if (name.length()==0) {
    		return "/";
    	}
    	else {
    		return name;
    	}
    }

    private RandomAccessContent getRaf(FileObject file, RandomAccessMode ram) throws FileSystemException {
    	if (ram.requestWrite()) {
    		if (file.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_WRITE)) {
    			return file.getContent().getRandomAccessContent(ram);
    		}
    		else {
    			return new PseudoWriteableRandomAccessContent(file, ram);
    		}
    	}
    	else {
    		if (file.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_READ)) {
    			return file.getContent().getRandomAccessContent(ram);
    		}
    		else {
    			return new PseudoRandomAccessContent(file, ram);
    		}
    	}
    }
    

}
