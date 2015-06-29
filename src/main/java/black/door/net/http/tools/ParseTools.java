package black.door.net.http.tools;

import black.door.struct.ByteQueue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nfischer on 6/10/15.
 */
public abstract class ParseTools {

    public static Map<String, String> parseHeaders(InputStream is) throws IOException {
        Map<String, String> headers = new HashMap<>();

        for(String next = nextLine(is); !next.isEmpty(); next = nextLine(is)){
            String[] header = next.split("\\s*:\\s*", 2);
            headers.put(header[0], header[1]);
        }
        return headers;
    }

    public static byte[] getBody(InputStream is, Map<String, String> headers) throws IOException {
        byte[] body;
        String transferEncoding = headers.get("Transfer-Encoding");
        if(transferEncoding != null && transferEncoding != "identity"){
            ByteQueue bq = new ByteQueue();
            for(String next = nextLine(is); !next.isEmpty(); next = nextLine(is)) {
                int size = Integer.valueOf(next, 16);
                if(size == 0)
                    break;
                byte[] chunk = new byte[size];
                is.read(chunk);
                bq.enQueue(chunk);
                nextLine(is);
            }
            body = bq.deQueue(bq.filled());

        }else{
            String contentLengthString = headers.get("Content-Length");
            if(contentLengthString != null){
                int contentLength = Integer.valueOf(contentLengthString);
                body = new byte[contentLength];
                is.read(body);
            }else{
                body = null;
            }
        }
        return body;
    }

    private static final char CR = 13;
    private static final char LF = 10;

    public static String nextLine(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean cr = false;
        for(int read = stream.read(); read != -1; read = stream.read()){
            char current = (char) read;
            if(current == CR){
                cr = true;
                continue;
            }
            if(cr){
                if(current == LF)
                    break;
                sb.append(CR);
                sb.append(current);
                cr = false;
                continue;
            }else{
                if(current == LF)
                    break;
                sb.append(current);
            }
        }

        return sb.toString();
    }
}
