package org.vfsutils.shell;

import org.vfsutils.shell.commands.Assert;
import org.vfsutils.shell.commands.Attrs;
import org.vfsutils.shell.commands.BoxedOpen;
import org.vfsutils.shell.commands.Cat;
import org.vfsutils.shell.commands.Cd;
import org.vfsutils.shell.commands.Cp;
import org.vfsutils.shell.commands.Dirs;
import org.vfsutils.shell.commands.Echo;
import org.vfsutils.shell.commands.Help;
import org.vfsutils.shell.commands.Load;
import org.vfsutils.shell.commands.Ls;
import org.vfsutils.shell.commands.MkDir;
import org.vfsutils.shell.commands.Mv;
import org.vfsutils.shell.commands.Ops;
import org.vfsutils.shell.commands.Peekd;
import org.vfsutils.shell.commands.Popd;
import org.vfsutils.shell.commands.Pushd;
import org.vfsutils.shell.commands.Pwd;
import org.vfsutils.shell.commands.Rem;
import org.vfsutils.shell.commands.Rm;
import org.vfsutils.shell.commands.Set;
import org.vfsutils.shell.commands.Touch;

public class BoxedCommandRegistry extends CommandRegistry {

	public BoxedCommandRegistry() {
		super();
		init();
	}
	
	public void init() {
		new Cd().register(this);
		new Pwd().register(this);
		new Pushd().register(this);
		new Popd().register(this);
		new Peekd().register(this);
		new Dirs().register(this);
		new Ls().register(this);
		new MkDir().register(this);
		new Rm().register(this);
		new Cp().register(this);
		new Mv().register(this);
		new Cat().register(this);
		new Attrs().register(this);
		new Ops().register(this);
		new Load().register(this);
		new Rem().register(this);
		new Help().register(this);
		new org.vfsutils.shell.commands.Error().register(this);
		new Set().register(this);
		new Touch().register(this);		
		new Assert().register(this);
		new Echo().register(this);
		
		new BoxedOpen().register(this);
	}

}
