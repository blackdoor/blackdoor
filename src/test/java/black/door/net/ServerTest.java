package black.door.net;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import black.door.net.ServerThread.ServerThreadBuilder;

public class ServerTest {

	public static final int SERVER_PORT = 9999;

	@Test
	public void testRun() throws IOException, URISyntaxException,
			InterruptedException {
		ServerThreadBuilder stb = new EchoThread.EchoThreadBuilder();
		Server server = new Server(stb, SERVER_PORT);
		new Thread(server).start();
		Thread.sleep(100);
		if (!server.isRunning()) {
			fail("Server was not started properly.");
		}
		server.stop();
		if (server.isRunning()) {
			fail("Server was not stopped properly.");
		}
	}

}
