package black.door.net.http.tools;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Created by nfischer on 6/9/15.
 */
public class HttpTest {

    @Test
    public void httpResponseTest() throws IOException {
        String response = "HTTP/1.0 200 OK\n" +
                "Date: Fri, 31 Dec 1999 23:59:59 GMT";
        System.out.println(new HttpResponse(response.getBytes(StandardCharsets.US_ASCII)));

        String responseWBody = "HTTP/1.0 200 OK\n" +
                "Date: Fri, 31 Dec 1999 23:59:59 GMT\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Content-Length: 32\n" +
                "\n" +
                "home=Cosby&favorite+flavor=flies";

        System.out.println(new HttpResponse(responseWBody.getBytes(StandardCharsets.US_ASCII)) + "\n");

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
        System.out.println(new HttpResponse(chunkedResponse.getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    public void httpRequestTest() throws IOException, URISyntaxException {
        String get = "GET /path/file.html HTTP/1.0\n" +
                "From: someuser@jmarshall.com\n" +
                "User-Agent: HTTPTool/1.0\n\n";
        System.out.println(new HttpRequest(get.getBytes(StandardCharsets.US_ASCII)));

        String post = "POST /path/script.cgi HTTP/1.0\n" +
                "From: frog@jmarshall.com\n" +
                "User-Agent: HTTPTool/1.0\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Content-Length: 32\n" +
                "\n" +
                "home=Cosby&favorite+flavor=flies";
        System.out.println(new HttpRequest(post.getBytes(StandardCharsets.US_ASCII)));

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
        System.out.println(new HttpRequest(chunked.getBytes(StandardCharsets.US_ASCII)));
    }

}