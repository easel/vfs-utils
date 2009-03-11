package org.vfsutils.shell.mina1;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.mina.common.IoSession;
import org.apache.mina.handler.StreamIoHandler;
	
public class EngineServerHandler extends StreamIoHandler {

	protected void processStreamIo(IoSession arg0, InputStream arg1,
			OutputStream arg2) {
		
		try {
			IoShell shell = new IoShell(arg1, arg2);
			shell.go();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
