package org.vfsutils.shell.remote;

import java.io.IOException;

import org.vfsutils.shell.Engine;

import bsh.ConsoleInterface;

public interface EngineRunner extends ConsoleInterface {

	public abstract void startEngine(Engine engine);

	public abstract ShellResponse stopEngine();

	public abstract ShellResponse handleInput(ShellRequest request)
			throws IOException;

}