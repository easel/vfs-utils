package org.vfsutils.shell.commands;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.CommandProvider;
import org.vfsutils.shell.Engine;

public class Attrs extends AbstractCommand implements CommandProvider {

	public Attrs() {
		super("attrs", new CommandInfo("Attribute interaction", "(info <path>| get <attrName> <path> | set <attrName> <attrValue> <path>)"));
	}

	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, FileSystemException {

		args.assertSize(2);

		String action = (String) args.getArguments().get(0);
		
		if (action.equalsIgnoreCase("get")){
			getAttr(args, engine);	        
		}
		else if (action.equalsIgnoreCase("set")) {
			setAttr(args, engine);
		}
		else if (action.equalsIgnoreCase("info")) {
			info(args, engine);
		}
		else {
			throw new IllegalArgumentException( action + " is not supported");
		}
	}
	
	protected void getAttr(Arguments args, Engine engine) throws IllegalArgumentException, FileSystemException {
		
		args.assertSize(3);		
        
		String attrName = (String)args.getArguments().get(1);
		String path = (String) args.getArguments().get(2);
		
		final FileObject[] files = engine.pathToFiles(path);
        
		if (files==null) {
        	throw new IllegalArgumentException("File does not exist " + path);
        }
		
		for (int i=0; i<files.length; i++) {
			getAttr(files[i], attrName, engine);
		}
		
	}
	
	protected void getAttr(FileObject file, String attrName, Engine engine) throws FileSystemException {
		
		Object attrValue = file.getContent().getAttribute(attrName);
    	String msg = " does not exist";
		if (attrValue==null) {
			if (file.getContent().hasAttribute(attrName)) {
				msg = ": ";
			}
		}
		else {
			msg = ": " + attrValue.toString();
		}
    	
		engine.println(attrName + msg);
	}

	
	protected void setAttr(Arguments args, Engine engine) throws IllegalArgumentException, FileSystemException {
		
		args.assertSize(4);
        
		String attrName = (String)args.getArguments().get(1);
		String attrValue = (String)args.getArguments().get(2);
		String path = (String) args.getArguments().get(3); 
		
		final FileObject[] files = engine.pathToFiles(path);
        
		if (files==null) {
        	throw new IllegalArgumentException("File does not exist " + path);
        }
		
		for (int i=0; i<files.length; i++) {
			setAttr(files[i], attrName, attrValue, engine);
		}	
		
	}
	
	protected void setAttr(FileObject file, String attrName, String attrValue, Engine engine) throws FileSystemException {
		
		if (file.getContent().hasAttribute(attrName)) {
			file.getContent().setAttribute(attrName, attrValue);
			engine.println(attrName + ": " + file.getContent().getAttribute(attrName));
		}
		else {
			engine.println(attrName + " does not exist");
		}
		
	}
	
	protected void info(Arguments args, Engine engine) throws IllegalArgumentException, FileSystemException {
		
		args.assertSize(2);
        
		String path = (String) args.getArguments().get(1);
		
		final FileObject[] files = engine.pathToFiles(path);
        
		if (files==null) {
        	throw new IllegalArgumentException("File does not exist " + path);
        }
		
		for (int i=0; i<files.length; i++) {
			info(files[i], engine);
		}		
		
	}
	
	protected void info(FileObject file, Engine engine) throws FileSystemException {
		
		FileContent content = file.getContent();
		Map attributes = content.getAttributes();
    	
    	Iterator keyIterator = attributes.keySet().iterator();
    	engine.println("Details of " + engine.toString(file));
    	if (file.getType().equals(FileType.FILE)) {
    		engine.println("  Size: " + content.getSize() + " bytes");
    	}
	    final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	    final String lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
	    engine.println("  Last modified: " + lastMod);
	    engine.println("  Is writeable: " + file.isWriteable());
    	engine.println("");
    	engine.println("  Attributes:");
    	while (keyIterator.hasNext()) {
    		Object key = keyIterator.next();
    		Object value = attributes.get(key);
    		engine.println("    " + key.toString() + " : " + (value!=null?value:""));
    	}
	}
	
	
}
