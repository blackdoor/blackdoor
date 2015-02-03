package blackdoor.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

public class SocketIOWrapper {
	private Socket sock;
	private BufferedInputStream in;
	private OutputStream out;
	private int bvffrSz;
	private float bvffrGrwthFctr;
	private Charset ncdng;
	
	public SocketIOWrapper(Socket sock) throws IOException{
		this(sock, 0, 0, null);
	}
	
	public SocketIOWrapper(Socket sock, float growthFactor) throws IOException{
		this(sock, growthFactor, 0, null);
	}
	
	public SocketIOWrapper(Socket sock, float growthFactor, int initialBufferSize) throws IOException{
		this(sock, growthFactor, initialBufferSize, null);
	}
	
	public SocketIOWrapper(Socket sock, float growthFactor, int initialBufferSize, Charset encoding) throws IOException{
		this.sock = sock;
		in = new BufferedInputStream(sock.getInputStream());
		out = sock.getOutputStream();
		setBufferGrowthFactor(growthFactor == 0 ? 1.25f : growthFactor);
		bvffrSz = initialBufferSize == 0 ? 256 : initialBufferSize;
		setEncoding(encoding == null ? Charset.forName("UTF-8") : encoding);
	}
	
	/**
	 * @return the encoding
	 */
	public Charset getEncoding() {
		return ncdng;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(Charset encoding) {
		this.ncdng = encoding;
	}

	/**
	 * @return the bufferGrowthFactor
	 */
	public float getBufferGrowthFactor() {
		return bvffrGrwthFctr;
	}

	/**
	 * @param bufferGrowthFactor the bufferGrowthFactor to set
	 */
	public void setBufferGrowthFactor(float bufferGrowthFactor) {
		this.bvffrGrwthFctr = bufferGrowthFactor;
	}

	public String read() throws IOException{
		//not feeling fantastic about this implementation, need to test and tweak buffer size and growth factor
		byte[] buffer = new byte[bvffrSz];
		int x;
		int filled;// = 0;
		for(filled = 0; (x = in.read()) != 0 && x != -1; filled ++){
			if(filled >= buffer.length){
				buffer = Arrays.copyOf(buffer, (int) Math.ceil(buffer.length * bvffrGrwthFctr));
			}
			buffer[filled] = (byte) x;
		}
		return new String(buffer, 0, filled, ncdng);		
	}
	
	public void flush() throws IOException{
		out.flush();
	}
	
	public byte[] write(Object s) throws IOException{
		byte[] ret = null;
		ret = String.valueOf(s).getBytes(ncdng);
		ret = Arrays.copyOf(ret, ret.length + 1);
		out.write(ret);
		return ret;
	}
	
	public Socket getSocket(){
		return sock;
	}
	
	/**
	 * close the underlying socket
	 * @throws IOException
	 */
	public void close() throws IOException{
		sock.close();
	}
	
}
