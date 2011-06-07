package org.vfsutils.shell.jline;

import java.util.ArrayList;
import java.util.List;

import jline.Completor;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.StringSplitter;

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
			
			StringSplitter splitter = engine.getCommandParser();
			
			FileObject parent;
			//should contain a normalized form (without non-significant quotes
			//and with escaped whitespace and significant quotes)
			String match;
			
			if (buffer==null || buffer.length()==0) {
				//empty buffer
				parent = engine.getCwd();
				match="";
				replaceAt = 0;
			}
			else {
				String workBuffer = buffer;
				//if the cursor comes before the end of the buffer then
				//only work on the part until the cursor
				if (cursor<buffer.length()) {
					//reduce to the cursor
					workBuffer = buffer.substring(0, cursor);
				}
				
				if (workBuffer.endsWith("/")) {
					//directory
					String filePath = splitter.removeQuotesAndEscapes(workBuffer);
					parent = engine.pathToFile(filePath);
					match="";
					replaceAt = cursor;
				}
				else if (workBuffer.indexOf("/")>-1) {
					//multilevel partial path
					String filePath = splitter.removeQuotesAndEscapes(workBuffer);
					FileObject selected = engine.pathToFile(filePath);
					parent = selected.getParent();
					match = splitter.escapeWhitespaceAndQuotes(selected.getName().getBaseName());
					replaceAt = workBuffer.lastIndexOf('/')+1;
				}
				else {
					//single level partial path
					parent = engine.getCwd();
					match = splitter.escapeWhitespaceAndQuotes(splitter.removeQuotesAndEscapes(workBuffer));
					replaceAt = 0;
				}
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
						name = splitter.escapeWhitespaceAndQuotes(name);
						
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
