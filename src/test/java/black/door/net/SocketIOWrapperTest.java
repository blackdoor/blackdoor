package black.door.net;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertTrue;

public class SocketIOWrapperTest {

	public SocketIOWrapper io1;
	public SocketIOWrapper io2;
	ServerSocket ss;

	@Before
	public void setUp() throws Exception {
		ss = new ServerSocket(1234);
		new Thread() {
			public void run() {
				try {
					io1 = new SocketIOWrapper(ss.accept());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e.getCause());
				}
			}
		}.start();

		io2 = new SocketIOWrapper(new Socket(InetAddress.getLoopbackAddress(), 1234));

	}

	@Test
	public void test() throws IOException {
		final String testString = "qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}qwertyuiop[]as\ndfghjklzxcvbnm{}";
		new Thread(){
			public void run(){
				String inString;
				try {
					inString = io1.read();
					assertTrue(inString.equals(testString));
					io1.write(inString);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
		io2.write(testString);
		String inString = io2.read();
		assertTrue(inString.equals(testString));
	}

	public void tearDown() throws IOException {
		io1.close();
		io2.close();
		ss.close();
	}

}
