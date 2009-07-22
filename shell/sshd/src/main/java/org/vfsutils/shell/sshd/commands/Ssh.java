package org.vfsutils.shell.sshd.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.vfs.FileSystemException;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.common.util.NoCloseInputStream;
import org.apache.sshd.common.util.NoCloseOutputStream;
import org.apache.tools.ant.util.ReaderInputStream;
import org.vfsutils.shell.Arguments;
import org.vfsutils.shell.CommandException;
import org.vfsutils.shell.Engine;
import org.vfsutils.shell.commands.AbstractCommand;

public class Ssh extends AbstractCommand {

	public Ssh() {
		super("ssh", "SSH Client", "[--port=<port>] --login=<login> <host> <command>*");
	}
	
	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {
		
		
		args.assertSize(1);
		
		int port = 22;
		String login;
		String host;
		String[] command;
		
		if (args.hasOption("port")) {
			port = Integer.parseInt(args.getOption("port"));
		}
		
		if (args.hasOption("login")) {
			login = args.getOption("login");
		}
		else {
			login = System.getProperty("user.name");
		}
		
		host = args.getArgument(0);
		
		if (args.size()>1) {
			command = new String[args.size()-1];
				
			for (int i=1; i<args.size(); i++) {
				command[i-1] = args.getArgument(i);
			}
		}
		else {
			command = null;
		}
		ssh(login, host, port, command, engine);
		

	}
	
	public void ssh(String login, String host, int port, String[] command, Engine engine) throws CommandException {
		
		engine.print("User: " + login);
		SshClient client = SshClient.setUpDefaultClient();
        client.start();
        try {
            ClientSession session = client.connect(host, port).await().getSession();

            int ret = ClientSession.WAIT_AUTH;
            while ((ret & ClientSession.WAIT_AUTH) != 0) {
                engine.print("Password:");
                BufferedReader r = new BufferedReader(engine.getConsole().getIn());
                String password = r.readLine();
                if (password.length()==0) {
                	throw new CommandException("Invalid password");
                }
                session.authPassword(login, password);
                ret = session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
            }
            if ((ret & ClientSession.CLOSED) != 0) {
                engine.error("error");
                throw new CommandException("Could not connect");                
            }
            ClientChannel channel;
            if (command == null) {
                channel = session.createChannel(ClientChannel.CHANNEL_SHELL);
                channel.setIn(new NoCloseInputStream(new ReaderInputStream(engine.getConsole().getIn())));
            } else {
                channel = session.createChannel(ClientChannel.CHANNEL_EXEC);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Writer w = new OutputStreamWriter(baos);
                for (String cmd : command) {
                    w.append(cmd).append(" ");
                }
                w.append("\n");
                w.close();
                channel.setIn(new ByteArrayInputStream(baos.toByteArray()));
            }
            channel.setOut(new NoCloseOutputStream(engine.getConsole().getOut()));
            channel.setErr(new NoCloseOutputStream(engine.getConsole().getErr()));
            channel.open().await();
            channel.waitFor(ClientChannel.CLOSED, 0);
            session.close(false);
        } catch (Exception e) {
			throw new CommandException(e);
		} finally {
            client.stop();
        }
	}

}
