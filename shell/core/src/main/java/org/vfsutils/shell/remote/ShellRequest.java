package org.vfsutils.shell.remote;

import java.io.Serializable;

public class ShellRequest implements Serializable {
	
	private static final long serialVersionUID = 699818347770190829L;

	private String in;
	
	public ShellRequest(String in) {
		this.in = in;
	}
	
	public ShellRequest() {
		this.in = "";
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}
}
