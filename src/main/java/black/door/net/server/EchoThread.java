package black.door.net.server;

import java.io.IOException;
import java.net.Socket;

import black.door.net.SocketIOWrapper;
import black.door.util.DBP;

public class EchoThread implements ServerThread {
	private Socket sock;

	private EchoThread() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		try {
			SocketIOWrapper io = new SocketIOWrapper(sock);
			io.write(io.read());
			io.close();
		} catch (IOException e) {
			DBP.printException(e);
		}
	}

	@Override
	public Socket getSocket() {
		return sock;
	}
	
	public static class EchoThreadBuilder implements ServerThreadBuilder{
		
		public EchoThreadBuilder(){
			
		}

		@Override
		public ServerThread build(Socket sock) {
			EchoThread ret = new EchoThread();
			ret.sock = sock;
			return ret;
		}
		
	}

}
