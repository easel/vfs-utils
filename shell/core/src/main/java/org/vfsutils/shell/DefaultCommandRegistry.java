package org.vfsutils.shell;

import org.vfsutils.shell.commands.*;

public class DefaultCommandRegistry extends CommandRegistry {

	public DefaultCommandRegistry() {
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
		new Bsh().register(this);
		new Open().register(this);
		new Close().register(this);
		new org.vfsutils.shell.commands.Error().register(this);
		new Set().register(this);
		new Touch().register(this);		
		new Assert().register(this);
		new Echo().register(this);
		new Register().register(this);
		new Md5().register(this);
		new Sync().register(this);
		new Hash().register(this);
		new Sysinfo().register(this);
	}
	
}
