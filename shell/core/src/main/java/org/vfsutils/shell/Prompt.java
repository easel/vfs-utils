package org.vfsutils.shell;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

public class Prompt {

	protected Context context;
	
	public Prompt(Context context) {
		this.context = context;
		if (this.context.get("PROMPT")==null) {
			this.context.set("PROMPT", "vfs > ");
		}
	}
	
	public String toString() {
		String prompt = (String) this.context.get("PROMPT");
		if (prompt.indexOf('\\')<0){
			return prompt;
		}
		else {
			try {
				FileName name = this.context.getCwd().getName();
				prompt = prompt.replaceAll("\\\\p", name.getPathDecoded());
				prompt = prompt.replaceAll("\\\\n", name.getFriendlyURI().replaceAll(":\\*+", ""));
			}
			catch (FileSystemException e) {
				//do nothing
			}
			return prompt;
		}
	}
	
}
