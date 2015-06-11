package black.door.net.http.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static black.door.net.http.tools.ParseTools.nextLine;
import static black.door.net.http.tools.ParseTools.parseHeaders;

/**
 * Created by nfischer on 6/10/15.
 */
public class HttpResponse implements HttpMessage{
    private Map<String, String> headers;
    private int statusCode;
    private String statusMessage;
    private String version;
    private byte[] body;

    public HttpResponse(String version, int statusCode, String statusMessage){
        this.version = version;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = new HashMap<>();
    }

    public HttpResponse(byte[] response) throws IOException {
        this(new ByteArrayInputStream(response));
    }

    public HttpResponse(InputStream stream) throws IOException {
        String firstLine = nextLine(stream);

        String[] split = firstLine.split("\\s+");
        version = split[0];
        statusCode = Integer.valueOf(split[1]);
        statusMessage = split[2];

        this.headers = parseHeaders(stream);

        this.body = ParseTools.getBody(stream, headers);
    }

    public HttpResponse putHeader(String key, String value){
        headers.put(key, value);
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(version);
        sb.append(" ");
        sb.append(statusCode);
        sb.append(" ");
        sb.append(statusMessage);
        sb.append('\n');

        for(Map.Entry<String, String> header : headers.entrySet()){
            sb.append(header.getKey());
            sb.append(": ");
            sb.append(header.getValue());
            sb.append('\n');
        }
        sb.append('\n');

        if(body != null)
            sb.append(new String(body, StandardCharsets.US_ASCII));

        return sb.toString();
    }
}
