package org.vfsutils.shell.mina;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.remote.CommandRunner;
import org.vfsutils.shell.remote.EngineRunner;
import org.vfsutils.shell.remote.ShellRequest;
import org.vfsutils.shell.remote.ShellResponse;
import org.vfsutils.shell.remote.StreamRunner;
import org.vfsutils.shell.remote.StreamRunner2;

public class ShellServerHandler extends IoHandlerAdapter {
		public void exceptionCaught(IoSession session, Throwable t) throws Exception {
			t.printStackTrace();
			session.close();
		}

		public void messageReceived(IoSession session, Object msg) throws Exception {
			String str = msg.toString();
			if( str.trim().equalsIgnoreCase("close") ||str.trim().equalsIgnoreCase("quit") || str.trim().equalsIgnoreCase("exit") || str.trim().equalsIgnoreCase("bye")) {
				session.close();
				return;
			}

			EngineRunner runner = (EngineRunner) session.getAttribute("engine");
			ShellResponse response = runner.handleInput(new ShellRequest(str.trim()));
			
			
			String result = response.getErr();
			if (result==null || result.length()==0) {
				result = response.getOut();
			}
			
			//if (!result.endsWith("\n")) result+="\n";
			//result += "#end#\n";
			session.write(result);
			
			System.out.println("Message written...");
		}

		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("Session created...");

			if( session.getTransportType() == TransportType.SOCKET )
				((SocketSessionConfig) session.getConfig() ).setReceiveBufferSize( 2048 );

	        session.setIdleTime( IdleStatus.BOTH_IDLE, 10 );
	        
	        EngineRunner runner = new StreamRunner();
			Engine remoteEngine = new Engine(runner);
			runner.startEngine(remoteEngine);
			
			session.setAttribute("engine", runner);
			
		}

		public void sessionClosed(IoSession session) throws Exception {
			EngineRunner runner = (EngineRunner) session.getAttribute("engine");
			runner.stopEngine();
			session.removeAttribute("engine");
		}
		
		

}
