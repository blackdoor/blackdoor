package black.door.net.http.tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nfischer on 6/6/15.
 */
public class HttpRequest implements HttpMessage{

    private Map<String, String> headers;
    private HttpVerb verb;
    private URI uri;
    private String version;
    private byte[] body;

    public HttpRequest(HttpVerb method, URI uri, String version){
        this.verb = method;
        this.uri = uri;
        this.version = version;
        headers = new HashMap<>();
    }

    @Deprecated
    public HttpRequest(Socket sock) throws IOException, URISyntaxException {
        this(new BufferedInputStream(sock.getInputStream()));
    }

    @Deprecated
    public HttpRequest(byte[] request) throws IOException, URISyntaxException {
        this(new ByteArrayInputStream(request));
    }

    @Deprecated
    public HttpRequest(InputStream is) throws IOException, URISyntaxException {

        String firstLine = ParseTools.nextLine(is);

        String[] split = firstLine.split("\\s+");
        verb = HttpVerb.valueOf(split[0]);
        uri = new URI(split[1]);
        version = split[2];

        headers = ParseTools.parseHeaders(is);

        body = ParseTools.getBody(is, headers, 8192);

    }

    public static HttpRequest parse(InputStream is, int maxBodySize) throws IOException, URISyntaxException, HttpParsingException {
        HttpVerb verb;
        URI uri;
        String version;
        Map<String, String> headers;

        String firstLine = ParseTools.nextLine(is);

        String[] split = firstLine.split("\\s+");
        if(split.length < 3)
            throw new HttpParsingException("Request line does not have 3 parts as described in RFC2616 5.1");

        verb = HttpVerb.valueOf(split[0]);
        uri = new URI(split[1]);
        version = split[2];

        headers = ParseTools.parseHeaders(is);

        byte[] body = ParseTools.getBody(is, headers, maxBodySize);

        HttpRequest ret = new HttpRequest(verb, uri, version);
        ret.setBody(body);

        return ret;
    }

    public HttpRequest putHeader(String headerName, String value){
        headers.put(headerName, value);
        return this;
    }

    public String getHeader(String headerName){
        return headers.get(headerName);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public HttpVerb getVerb() {
        return verb;
    }

    public void setVerb(HttpVerb verb) {
        this.verb = verb;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(verb);
        sb.append(" ");
        sb.append(uri.toString());
        sb.append(" ");
        sb.append(version);
        sb.append("\n");
        for(Map.Entry<String, String> e : headers.entrySet()){
            sb.append(e.getKey());
            sb.append(": ");
            sb.append(e.getValue());
            sb.append("\n");
        }
        sb.append("\n");
        if(body != null)
            sb.append(new String(body, StandardCharsets.UTF_8));

        return sb.toString();
    }
}
