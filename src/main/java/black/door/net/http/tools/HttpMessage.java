package black.door.net.http.tools;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by nfischer on 6/10/15.
 */
public interface HttpMessage {
    byte[] getBody();
    String getVersion();
    HttpMessage putHeader(String headerName, String value);
    String getHeader(String headerName);
}
