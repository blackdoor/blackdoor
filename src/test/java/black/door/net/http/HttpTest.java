package black.door.net.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import black.door.net.http.tools.HttpRequest;
import black.door.net.http.tools.HttpResponse;
import black.door.net.http.tools.HttpVerb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by nfischer on 6/9/15.
 */
public class HttpTest {

    @Test
    public void httpResponseTest() throws IOException {
        String responsestring = "HTTP/1.0 200 OK\n" +
                "Date: Fri, 31 Dec 1999 23:59:59 GMT";
        
        HttpResponse response = new HttpResponse(responsestring.getBytes(StandardCharsets.US_ASCII));
    	Map<String, String> mp = response.getHeaders();
    	
		assertEquals( "HTTP/1.0",response.getVersion());
		assertEquals( 200,response.getStatusCode());
		assertEquals("Fri, 31 Dec 1999 23:59:59 GMT",mp.get("Date"));
        
        String responseWBodystring = "HTTP/1.0 200 OK\n" +
                "Date: Fri, 31 Dec 1999 23:59:59 GMT\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Content-Length: 32\n" +
                "\n" +
                "home=Cosby&favorite+flavor=flies";
        
        HttpResponse responseWBody = new HttpResponse(responseWBodystring.getBytes(StandardCharsets.US_ASCII));
        mp = responseWBody.getHeaders();
        
		assertEquals( "HTTP/1.0",responseWBody.getVersion());
		assertEquals( 200,responseWBody.getStatusCode());
		assertEquals("Fri, 31 Dec 1999 23:59:59 GMT",mp.get("Date"));
		assertEquals("32",mp.get("Content-Length"));
		assertEquals("application/x-www-form-urlencoded",mp.get("Content-Type"));
		assertEquals("home=Cosby&favorite+flavor=flies",new String(responseWBody.getBody()));
        
        String chunkedResponse = "HTTP/1.0 200 OK\n" +
                "Date: Fri, 31 Dec 1999 23:59:59 GMT\n" +
                "Transfer-Encoding: nop\n" +
                "\n" +
                "4\r\n" +
                "Wiki\r\n" +
                "5\r\n" +
                "pedia\r\n" +
                "e\r\n" +
                " in\r\n\r\nchunks.\r\n" +
                "0\r\n" +
                "\r\n";
        
		assertEquals( "HTTP/1.0",responseWBody.getVersion());
		assertEquals( 200,responseWBody.getStatusCode());
		assertEquals("Fri, 31 Dec 1999 23:59:59 GMT",mp.get("Date"));
		assertEquals("32",mp.get("Content-Length"));
		assertEquals("application/x-www-form-urlencoded",mp.get("Content-Type"));
		//TODO 
    }

    @Test
    public void httpRequestTest() throws IOException, URISyntaxException {
       // GET
    	String getrequeststring = "GET /path/file.html HTTP/1.0\n" +
                "From: frog@jmarshall.com\n" +
                "User-Agent: HTTPTool/1.0\n\n";
		
        HttpRequest get = new HttpRequest(getrequeststring.getBytes(StandardCharsets.US_ASCII));
    	Map<String, String> mp = get.getHeaders();
    	
        assertEquals(HttpVerb.GET,get.getVerb());
		assertEquals("/path/file.html",get.getUri().toString());
		assertEquals( "HTTP/1.0",get.getVersion());
		assertEquals("frog@jmarshall.com",mp.get("From"));
		assertEquals("HTTPTool/1.0",mp.get("User-Agent"));
        
		//POST
        String postrequeststring = "POST /path/script.cgi HTTP/1.0\n" +
                "From: frog@jmarshall.com\n" +
                "User-Agent: HTTPTool/1.0\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Content-Length: 32\n" +
                "\n" +
                "home=Cosby&favorite+flavor=flies";
		
        HttpRequest post = new HttpRequest(postrequeststring.getBytes(StandardCharsets.US_ASCII));
		mp = post.getHeaders();
	
		assertEquals(HttpVerb.POST,post.getVerb());
		assertEquals("/path/script.cgi",post.getUri().toString());
		assertEquals( "HTTP/1.0",post.getVersion());
		assertEquals("frog@jmarshall.com",mp.get("From"));
		assertEquals("HTTPTool/1.0",mp.get("User-Agent"));
		assertEquals("application/x-www-form-urlencoded",mp.get("Content-Type"));
		assertEquals( "32",mp.get("Content-Length"));
		assertEquals("home=Cosby&favorite+flavor=flies",new String(post.getBody()));

		// Chunked POST
        String chunked = "POST /path/script.cgi HTTP/1.0\n" +
                "From: frog@jmarshall.com\n" +
                "User-Agent: HTTPTool/1.0\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Transfer-Encoding: nop\n" +
                "\n" +
                "4\r\n" +
                "Wiki\r\n" +
                "5\r\n" +
                "pedia\r\n" +
                "e\r\n" +
                " in\r\n\r\nchunks.\r\n" +
                "0\r\n" +
                "\r\n";
       
        HttpRequest chunkedpost = new HttpRequest(chunked.getBytes(StandardCharsets.US_ASCII));
		mp = chunkedpost.getHeaders();
		assertEquals(HttpVerb.POST,chunkedpost.getVerb());
		assertEquals("/path/script.cgi",chunkedpost.getUri().toString());
		assertEquals( "HTTP/1.0",chunkedpost.getVersion());
		assertEquals("frog@jmarshall.com",mp.get("From"));
		assertEquals("HTTPTool/1.0",mp.get("User-Agent"));
		assertEquals("nop",mp.get("Transfer-Encoding"));
		assertEquals("application/x-www-form-urlencoded",mp.get("Content-Type"));
		//TODO
    }

}