package black.door.net.http.tools;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by nfischer on 7/19/2015.
 */
public class ParseToolsTest {

	@Test
	public void testNextLine() throws Exception {
		InputStream is = new ByteArrayInputStream("Hello world\n".getBytes());
		assertTrue(ParseTools.nextLine(is).equals("Hello world"));
		try {
			ParseTools.nextLine(is);
			fail();
		}catch (EOFException e){}

	}
}