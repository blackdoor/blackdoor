package black.door.net.server;

import java.net.Socket;

public interface ServerThread extends Runnable {
	public Socket getSocket();
	public interface ServerThreadBuilder {
		public abstract ServerThread build(Socket sock);
	}
}
