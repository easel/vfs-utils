package org.vfsutils.shell.jline;

import java.util.ArrayList;
import java.util.List;

import jline.Completor;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.vfsutils.shell.Engine;

/**
 * VFS FileName completion for JLine. It supports case sensitiveness and
 * multilevel completion. It uses a simple internal caching system that 
 * keeps the list of the names of the last retrieved children to speed up 
 * drilling down in the same directory. This caching can be disabled with
 * the cacheLast property.
 * 
 * @author kleij -at- users.sourceforge.net
 *
 */
public class VfsFileNameCompletor implements Completor {

	protected Engine engine;
	protected boolean caseSensitive;
	
	private boolean cacheLast = true;	
	protected FileName lastName = null;
	protected List lastChildren = new ArrayList();
	
	public VfsFileNameCompletor(Engine engine, boolean caseSensitive) {
		this.engine = engine;
		this.caseSensitive = caseSensitive;
	}
	
	/**
	 * Completes the filename. It support multiple levels of the path.
	 */
	public int complete(String buffer, int cursor, List candidates) {
		
		int replaceAt = 0;
		try {
				
			FileObject parent;
			String match;
			
			if (buffer==null || buffer.length()==0) {
				//empty buffer
				parent = engine.getCwd();
				match="";
				replaceAt = cursor;
			}
			else if (buffer.endsWith("/")) {
				//directory
				parent = engine.pathToFile(buffer);
				match="";
				replaceAt = cursor;
			}
			else if (buffer.indexOf("/")>-1) {
				//multilevel partial path
				FileObject selected = engine.pathToFile(buffer);
				parent = selected.getParent();
				match = selected.getName().getBaseName();
				replaceAt = buffer.lastIndexOf('/')+1;
			}
			else {
				//single level partial path
				parent = engine.getCwd();
				match = buffer;
				replaceAt = 0;
			}
	
			//if the parent does not exist there can be no completion
			if (parent.exists()) {
				
				//use the cached list if the name is the same as the last time
				if (cacheLast && parent.getName().equals(this.lastName)) {
					for (int i=0; i<lastChildren.size(); i++) {
						String name = (String) lastChildren.get(i);
						if (match(name, match)) {
							candidates.add(name);
						}
					}
				}
				else {
					//reset the cached information
					if (cacheLast) {
						this.lastName = parent.getName();
						this.lastChildren.clear();					
					}
					
					//retrieve children and iterate
					FileObject[] children = parent.getChildren();
					for (int i=0; i<children.length; i++) {
						FileObject child = children[i];

						String name = child.getName().getBaseName() + (child.getType().equals(FileType.FOLDER)?"/":"");
						name = engine.escapeWhitespace(name);
						
						//cache
						if (cacheLast) this.lastChildren.add(name);
						
						//match
						if (match(name,match)) {
							candidates.add(name);
						}
					}
				}
			}
		} catch (FileSystemException e) {
			//ignore for now
		}
		return replaceAt;
	}
	
	/**
	 * Tests whether the input starts with the given prefix
	 * If the prefix is a zero-length string a match is assumed.
	 * The case sensitiveness of the comparison is governed by
	 * the caseSensitive property.
	 * @param input the string to test with
	 * @param prefix the value with which the input should start
	 * @return true if the input starts with the prefix
	 */
	protected boolean match(String input, String prefix) {
		if (prefix.length()==0) {
			return true;
		}
		else if (this.caseSensitive) {
			return input.startsWith(prefix);
		}
		else {
			return input.toLowerCase().startsWith(prefix.toLowerCase());
		}
	}

	public boolean isCacheLast() {
		return cacheLast;
	}

	public void setCacheLast(boolean cacheLast) {
		this.cacheLast = cacheLast;
	}
	
	
	
}
