package black.door.net;

import java.net.Socket;

/**
 * A ServerThread that does nothing. It has an empty run method and a null socket.
 * @author nfischer3
 *
 */
public class NullThread implements ServerThread{
	
	public NullThread(){
		
	}

	@Override
	public void run() {
	}

	@Override
	public Socket getSocket() {
		return null;
	}
}
