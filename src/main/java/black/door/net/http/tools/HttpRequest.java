package black.door.net.http.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nfischer on 6/6/15.
 */
public class HttpRequest implements HttpMessage{

    private Map<String, String> headers;
    private HttpVerb verb;
    private URL url;
    private String version;
    private byte[] body;

    public HttpRequest(HttpVerb method, URL url, String version){
        this.verb = method;
        this.url = url;
        this.version = version;
        headers = new HashMap<>();
    }

    public HttpRequest(Socket sock) throws IOException, URISyntaxException {
        this(sock.getInputStream());
    }

    public HttpRequest(byte[] request) throws IOException, URISyntaxException {
        this(new ByteArrayInputStream(request));
    }

    public HttpRequest(InputStream is) throws IOException, URISyntaxException {

        String firstLine = ParseTools.nextLine(is);

        String[] split = firstLine.split("\\s+");
        verb = HttpVerb.valueOf(split[0]);
        url = new URL(split[1]);
        version = split[2];

        headers = ParseTools.parseHeaders(is);

        body = ParseTools.getBody(is, headers);

    }

    public HttpRequest putHeader(String headerName, String value){
        headers.put(headerName, value);
        return this;
    }

    @Override
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

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL uri) {
        this.url = uri;
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
        sb.append(url.toString());
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
