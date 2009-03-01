package org.vfsutils.shell.remote;

import java.io.Serializable;

public class ShellResponse implements Serializable {
	
	private static final long serialVersionUID = -2652538176869900889L;
	
	private String out;
	private String err;
	
	public ShellResponse(String out, String err) {
		this.out = out;
		this.err = err;
	}
	
	public ShellResponse() {
		this.out="";
		this.err="";
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public String toString() {
		if (this.err!=null && this.err.length()>0) {
			return this.err;
		}
		else if (this.out!=null){
			return this.out;
		}
		else {
			return "";
		}
	}
	
}
