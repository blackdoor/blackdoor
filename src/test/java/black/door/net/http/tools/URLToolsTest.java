package black.door.net.http.tools;

import black.door.util.DBP;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by nfischer on 6/28/2015.
 */
public class URLToolsTest {

	URL googleSearch;
	URL jsSite;

	@Before
	public void setUp() throws Exception {
		googleSearch = new URL("https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=sample%20url&other=thing");
		jsSite = new URL("http://www.madebysofa.com/#people");
		DBP.VERBOSE = true;
	}

	@Test
	public void testParseQueries() throws Exception {
		Map queries = URLTools.parseQueries(googleSearch);
		System.out.println(queries);
		assertTrue(queries.get("sourceid").equals("chrome-instant"));
		assertTrue(queries.get("ion").equals("1"));
		assertTrue(queries.get("espv").equals("2"));
		assertTrue(queries.get("ie").equals("UTF-8"));

		try {
			assertTrue(URLTools.parseQueries(jsSite).isEmpty());
			fail("expected exception");
		}catch (NullPointerException e){

		}
	}

	@Test
	public void testParseRef(){
		Map refs = URLTools.parseRef(googleSearch);
		System.out.println(refs);
		assertTrue(refs.get("q").equals("sample%20url"));

		Map anchor = URLTools.parseRef(jsSite);
		assertTrue(anchor.get("id").equals("people"));
	}
}