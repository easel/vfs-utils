package org.vfsutils.shell.commands;

import java.util.LinkedList;
import java.util.List;

import org.vfsutils.shell.CommandInfo;
import org.vfsutils.shell.Engine;

public abstract class AbstractOpenClose extends AbstractCommand {

	public AbstractOpenClose(String cmd, CommandInfo info) {
		super(cmd, info);
	}

	protected List getOpenFs(Engine engine) {
		List openFs = (List)engine.getContext().get("vfs.openfs");
	    if (openFs==null) {
	    	openFs = new LinkedList();
	    	engine.getContext().set("vfs.openfs", openFs);
	    }
	    return openFs;
	}

}