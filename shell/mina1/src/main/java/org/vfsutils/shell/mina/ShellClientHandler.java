package org.vfsutils.shell.mina;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.vfsutils.shell.Engine;

public class ShellClientHandler extends IoHandlerAdapter {

	private String host;
	private int port;
	private SocketConnector connector;
	private IoSession session;

	private Engine engine;

	public ShellClientHandler(String host, int port, Engine engine) {
		this.host = host;
		this.port = port;
		this.engine = engine;

		connector = new SocketConnector();
		connector.getFilterChain().addLast(
				"codec",
				new ProtocolCodecFilter(new ShellLineCodecFactory(Charset
						.forName("UTF-8"))));
	}

	public void connect() {
		ConnectFuture connectFuture = connector.connect(new InetSocketAddress(
				host, port), this);
		connectFuture.join(5000);
		try {
			session = connectFuture.getSession();
		} catch (RuntimeIOException e) {
			engine.error(e);
		}
	}

	public void disconnect() {
		if (session != null) {
			session.close().join(30);
			session = null;
		}
	}

	public void sendRequest(String request) {
        if (session == null) {
            engine.error("not connected");
        }
        else {
            session.write(request);
            
            synchronized (engine) {
            	try {
					engine.wait(30000);
				} catch (InterruptedException e) {
					//ignore
				}
            }
        }
        
    }

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		engine.println(message);
		
		synchronized (engine) {
			engine.notify();
		}
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		engine.error(cause);
	}

}
