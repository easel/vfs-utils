package org.vfsutils.shell.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class ShellServer {

	public static void main(String[] args) throws IOException {
		
		int port = 9123;
		
		if (args.length>0) {
			port = Integer.parseInt(args[0]);
		}		 
		
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        IoAcceptor acceptor = new SocketAcceptor();

        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getFilterChain().addLast( "logger", new LoggingFilter() );
        cfg.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new ShellLineCodecFactory( Charset.forName( "UTF-8" ))));

        acceptor.bind( new InetSocketAddress(port), new ShellServerHandler(), cfg);
        System.out.println("MINA shell server started.");
    }

	
}
