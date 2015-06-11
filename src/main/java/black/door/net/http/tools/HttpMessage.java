package black.door.net.http.tools;

import java.util.Map;

/**
 * Created by nfischer on 6/10/15.
 */
public interface HttpMessage {
    public Map<String, String> getHeaders();
    public byte[] getBody();
    public String getVersion();
}
