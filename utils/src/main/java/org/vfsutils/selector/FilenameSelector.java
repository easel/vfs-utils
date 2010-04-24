package org.vfsutils.selector;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.UriParser;

public class FilenameSelector implements FileSelector {
	
	private String pattern = null;
    private boolean casesensitive = true;
    protected boolean negated = false;
    
    private int maxDepth = -1;
	private boolean includeBaseFolder = false;
	
	private boolean includeFiles = true;
	private boolean includeFolders = true;
    
    
    /**
     * The name of the file, or the pattern for the name, that
     * should be used for selection.
     *
     * @param pattern the file pattern that any filename must match
     *                against in order to be selected.
     */
    public void setName(String pattern) throws FileSystemException {
        pattern = pattern.replace('/', FileName.SEPARATOR_CHAR).replace('\\',
                FileName.SEPARATOR_CHAR);
        if (pattern.endsWith(FileName.SEPARATOR)) {
            pattern += "**";
        }
        
        pattern = normalizePath(pattern);
        
        this.pattern = pattern;
        
        //count the depth 
        if (this.maxDepth<0){
        	this.maxDepth = calculateDepth(pattern);
        }
        
    }
    
    
    private int calculateDepth(String pattern) throws FileSystemException {
    	if (pattern.indexOf("**/")>-1) {
        	//a directory wildcard means no restriction
        	return -1;
        }
        else {
        	return pattern.split("/").length -1;
        }
    }
    
    /**
     * Replace '../' by '', '/./' bye '', '//' by '/'
     * @param pattern
     * @return
     */
    private String normalizePath(String pattern) throws FileSystemException {
    	StringBuffer buffer = new StringBuffer(pattern);
    	UriParser.normalisePath(buffer);
    	return buffer.toString();
    }
    
    /**
     * Whether to ignore case when checking filenames.
     *
     * @param casesensitive whether to pay attention to case sensitivity
     */
    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }

    /**
     * You can optionally reverse the selection of this selector,
     * thereby emulating an &lt;exclude&gt; tag, by setting the attribute
     * negate to true. This is identical to surrounding the selector
     * with &lt;not&gt;&lt;/not&gt;.
     *
     * @param negated whether to negate this selection
     */
    public void setNegate(boolean negated) {
        this.negated = negated;
    }
    
    /**
     * By default the base folder is excluded from the selection, but you can choose
     * to include it. When the base folder is included the negate option is applied
     * to the result; when it is excluded not.
     * @param excluded
     */
    public void setIncludeBaseFolder(boolean included) {
    	this.includeBaseFolder = included;
    }
    
    /**
     * By default the maximum depth is related to the name pattern that is given. It 
     * can be constrained further by this method
     * @param maxDepth
     */
    public void setMaxDepth(int maxDepth) {
    	this.maxDepth = maxDepth;
    }
    
    public void setIncludeFiles(boolean includeFiles) {
    	this.includeFiles = includeFiles;
    }
    
    public void setIncludeFolders(boolean includeFolders) {
    	this.includeFolders = includeFolders;
    }
    
	public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
		boolean result = true;
		if (fileInfo.getDepth()==0 && !includeBaseFolder && fileInfo.getBaseFolder().equals(fileInfo.getFile())) {
			result = false;
		}
		
		if (result) {
			result = (includeType(fileInfo.getFile().getType(), this.includeFiles, this.includeFolders) == !negated);
		}
		
		if (result) {
			result = (includeName(fileInfo.getBaseFolder().getName().getRelativeName(fileInfo.getFile().getName()), this.pattern, this.casesensitive) == !negated);
		}
		return result;
	}
	
	protected boolean includeType(FileType type, boolean includeFiles, boolean includeFolders) {
		boolean result = false;
		if (includeFiles && includeFolders) {
			result = true;
		}
		else if (includeFiles){
			result = type.equals(FileType.FILE) || type.equals(FileType.FILE_OR_FOLDER);
		}
		else if (includeFolders){
			result = type.equals(FileType.FOLDER) || type.equals(FileType.FILE_OR_FOLDER);
		}
		return result;
	}
	
	protected boolean includeName(String relativeName, String constraint, boolean caseSensitive) {
		return SelectorUtils.matchPath(pattern, relativeName, caseSensitive) ;
	}
	
	public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
		if (this.maxDepth==-1) {
			return true;
		}
		else {
			return fileInfo.getDepth()<=this.maxDepth;
		}
	}
	
	

}
