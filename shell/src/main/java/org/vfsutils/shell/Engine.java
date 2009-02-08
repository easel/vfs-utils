package org.vfsutils.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.vfsutils.selector.FilenameSelector;

import bsh.ConsoleInterface;

public class Engine {
	
	protected ConsoleInterface console;
	protected CommandRegistry commandRegistry;
	protected FileSystemManager mgr;
	protected Context context;
	
	protected CommandParser commandParser;
	
    private boolean echoOn = false;
    private boolean haltOnError = false;
    private Exception lastError = null;
    
    	
	
	public Engine(ConsoleInterface console) throws FileSystemException {
		this(console, new DefaultCommandRegistry(), VFS.getManager());
	}
	
	public Engine(ConsoleInterface console, CommandRegistry reg, FileSystemManager mgr) throws FileSystemException {
		this.console = console;
		this.commandRegistry = reg;
		this.mgr = mgr;
		this.context = new Context(mgr);
		this.commandParser = new MultilineCommandParser();
	}
	
	public void go() throws Exception {

		BufferedReader in = new BufferedReader( console.getIn() );
		
		while (true) {
			
			try {
				console.print(this.getPrompt());
				
				final Arguments  args = nextCommand(in);
	            if (args == null) {
	                return;
	            }
	            if (!args.hasCmd()) {
	                continue;
	            }
	            
	            final String cmd = args.getCmd();
	            if (cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("bye")) {
	                return;
	            }
	            else if (cmd.startsWith("#")) {
	            	continue;
	            }
            
                boolean success = handleCommand(args);
                if (!success && haltOnError) {
                	return;
                }
            }
            catch (final Exception e)
            {
                error(e.getMessage());
                lastError = e;
                if (haltOnError) {
                	return;
                }
            }
		}
	}

	/**
	 * Loads the commands from the given reader.
	 * Note that it does not close the reader.
	 * @param reader
	 * @throws Exception
	 */
	public void load(Reader reader) throws Exception {
		BufferedReader in = new BufferedReader( reader );
		
		while (true) {
			final Arguments args = nextCommand(in);
            if (args == null) {
                return;
            }
            if (!args.hasCmd()) {
                continue;
            }
                        
            final String cmd = args.getCmd();
            if (cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("bye")) {
            	return;
            }
            else if (cmd.startsWith("#")) {
            	continue;
            }
            
            try {
                boolean success = handleCommand(args);
                if (!success && haltOnError) {
                	return;
                }
            }
            catch (final Exception e)
            {
                console.error(e.getMessage());
                lastError = e;
                if (haltOnError) {
                	return;
                }
            }
		}
	}
	
	
	public void close() throws FileSystemException {
    	
		//let the close command handle the open filesystems
		try {
			handleCommand("close -a");
		}
		catch (Exception e) {
			error("Error while closing down: " + e.getMessage());
		}
		
    	FileSystem fs = getCwd().getFileSystem();
    	
    	while (fs!=null) {
    		this.mgr.closeFileSystem(fs);
    		FileObject parent = fs.getParentLayer();
    		if (parent==null) {
    			fs = null;
    		} else {
    			fs = parent.getFileSystem();
    		}
    	}
    }

	/**
     * Returns the next command, split into tokens.
     */
    private Arguments nextCommand(BufferedReader in) throws IOException {
        final String line = in.readLine();
        if (line == null) {
            return null;
        }
        
        if (this.isEchoOn()) {
        	console.println(line);
        }
        
        //resolve variables
        String resolvedLine = resolveVariables(line); 
        return commandParser.parse(resolvedLine);
    }
    
    public boolean handleCommand(final String commandString) throws Exception {
    	String resolvedLine = resolveVariables(commandString);
    	Arguments args = commandParser.parse(resolvedLine);
    	return handleCommand(args);
    }
    
    /**
     * Handles a command.
     */
    protected boolean handleCommand(final Arguments args) throws Exception {    	
    	
    	if (args.hasCmd()) {
    		String cmd = args.getCmd();
    		CommandProvider command;
    		
			if ((command = (CommandProvider)this.commandRegistry.getCommand(cmd))!=null) {
				try {
					command.execute(args, this);
				}
	    		catch (IllegalArgumentException e) {
	    			error(e.getMessage());
	    			lastError = e;
	    			if (haltOnError) {
	    				return false;
	    			}
	    		}
	    		catch (CommandException e) {
	    			error(e.getMessage());
	    			lastError = e;
	    			if (haltOnError) {
	    				return false;
	    			}
	    		}
	    		catch (FileSystemException e) {
	    			error(e.getMessage());
	    			lastError = e;
	    			if (haltOnError) {
	    				return false;
	    			}
	    		}
			} 
			else {
				error("Unknown command " + cmd);
				if (haltOnError) {
					return false;
				}
			}
    	}
    	return true;
    }

    protected String resolveVariables(final String cmd) throws IllegalArgumentException {
    	    	
    	//first check
    	if (cmd.indexOf('$') == -1) {
    		return cmd;
    	}
    	
    	String result = cmd;
    	//example variable is $a_B
    	Pattern p = Pattern.compile("(\\$\\w+)");
    	Matcher m = p.matcher(cmd);
    	
    	//get the variables, a set is used to remove duplicates
    	Set vars = new HashSet(5);
    	
    	while (m.find()) {
    		//we only have one group in the pattern
    		String group = m.group();
    		vars.add(group);
    	}
    	
    	//replace the variables by their values
    	Iterator iterator = vars.iterator();
    	while (iterator.hasNext()) {
    		String var = (String) iterator.next();
    		Object value = this.getContext().get(var.substring(1));
    		//only replace matched values
    		if (value!=null) {
    			//escape the dollar, because replaceAll works on regexp
    			result = result.replaceAll("\\"+var, value.toString());
    		}
    		else {
    			throw new IllegalArgumentException("Unbound variable " + var);
    		}
    	}
    	
    	return result;    	
    	
    }
    
    protected String[] resolveVariables(final String cmd[]) {
    	String[] result = new String[cmd.length];
    	for (int i=0; i<cmd.length; i++) {
    		String s = cmd[i];
    		if (s.startsWith("$")) {
    			//try to resolve
    			Object value = this.getContext().get(s.substring(1));
    			if (value==null) {
    				//not found, pass as is
    				result[i] = s;
    			}
    			else {
    				result[i] = value.toString();
    			}
    		}
    		else {
    			result[i] = s;
    		}
    	}
    	return result;
    }
    
	public FileSystemManager getMgr() {
		return mgr;
	}
	
	public void setMgr(FileSystemManager mgr) {
		this.mgr = mgr;
	}

	public ConsoleInterface getConsole() {
		return console;
	}
	
	
	public CommandRegistry getCommandRegistry() {
		return commandRegistry;
	}


	public void setCommandRegistry(CommandRegistry commandRegistry) {
		this.commandRegistry = commandRegistry;
	}

	public Context getContext() {
		return this.context;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	
	public FileObject getCwd() {
		return this.context.getCwd();
	}
	
	public Prompt getPrompt() {
		return this.context.getPrompt();
	}

	public Exception getLastError() {
		return this.lastError;
	}
	
	public void println(Object o) {
		this.console.println(o);
	}
	
	public void print(Object o) {
		this.console.print(o);
	}
	
	public void error(Object o) {
		this.console.getErr().println(o);
		this.console.getErr().flush();
	}
	
	
	public boolean isEchoOn() {
		return echoOn;
	}


	public void setEchoOn(boolean echoOn) {
		this.echoOn = echoOn;
	}
	
	public boolean isHaltOnError() {
		return this.haltOnError;
	}
	
	public void setHaltOnError (boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

	/**
	 * Utility method to print the name of a file object.
	 * Needed since the VFS FileName.toString prints passwords.
	 * @param file
	 * @return
	 */
	public String toString(FileObject file) {
		String result = "";
		if (file!=null) {
			result = toString(file.getName());
		}
		return result;
	}
	
	public String toString(FileName filename) {
		String result = "";
		if (filename!=null) {
			String nameString = filename.getFriendlyURI();
			//strip the password hiding (:*****)
			result = nameString.replaceAll(":\\*+", "");
		}
		return result;
	}
	
	/**
	 * Resolves the path to a file. Note that the resolved file
	 * might not actually exist, only the name is resolved.
	 * @param path
	 * @return
	 * @throws FileSystemException
	 */
	public FileObject pathToFile(String path) throws FileSystemException {
		
		FileObject tmp;
		//if (path.indexOf("://")!=-1) {
		//	tmp = getMgr().resolveFile(path);
		//}
		//else {
			tmp = getMgr().resolveFile(getCwd(), path);
		//}
		return tmp;
	}
	
	
	/**
	 * Resolves the path to a file. If the file does not exist an
	 * IllegalArgumentException is thrown.
	 * @param path
	 * @return
	 * @throws FileSystemException
	 * @throws IllegalArgumentException
	 */
	public FileObject pathToExistingFile(String path) throws FileSystemException, IllegalArgumentException {
		FileObject file = this.pathToFile(path);
		
		if (!file.exists()) {
        	throw new IllegalArgumentException("File does not exist " + this.toString(file));
        }
		
		return file;
	}
	
	/**
	 * Resolves files. The returned files (or folders) are guaranteed to exist.
	 * @param pathPattern
	 * @return
	 * @throws FileSystemException
	 */
	public FileObject[] pathToFiles(String pathPattern) throws FileSystemException, IllegalArgumentException {		
		
		if (pathPattern.indexOf('*')>-1) {
			FilenameSelector selector = new FilenameSelector();
			selector.setName(pathPattern);
			
			List selected = new ArrayList();
			getCwd().findFiles(selector, false, selected);
			
			FileObject[] array = (FileObject[])selected.toArray(new FileObject[selected.size()]);
			return array;
		}
		else {
			FileObject file = this.pathToExistingFile(pathPattern);
			return new FileObject[]{file};
		}
		
	}

}
