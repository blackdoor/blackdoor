/**
 * 
 */
package blackdoor.crypto;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import blackdoor.util.Misc;

/**
 * @author nfischer3
 * Secure Hash Encryption. SHA256 in CTR mode implemented with methods similar to the standard Crypto.java library.
 * uses SHE256 v1.1 (H(key || incremented IV) instead of H(key XOR incremented IV))
 */
public class SHE {
	
	public int blockSize;// = 32;
	private int blockNo;
	private byte[] IV;
	private byte[] key;
	private boolean cfg;
	private ByteBuffer buffer;
	//private int bufferIndex; //index at which to place next byte in buffer
	private MessageDigest mD;
	
	/**
	 * Creates a Cipher object with specified algorithm.
	 * @param algorithm the algorithm to use for this cipher.
	 */
	public SHE(String algorithm) throws NoSuchAlgorithmException{
		blockNo = 0;
		cfg = false;
		mD = MessageDigest.getInstance(algorithm);
		blockSize = mD.getDigestLength();
		buffer = ByteBuffer.allocate(blockSize);
	}
	
	/**
	 * Creates a Cipher object.
	 */
	public SHE(Algorithm algorithm){
		blockNo = 0;
		cfg = false;
		try {
			mD = MessageDigest.getInstance(algorithm.getAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		blockSize = mD.getDigestLength();
		buffer = ByteBuffer.allocate(blockSize);
	}
	/**
	 * Creates a Cipher object.
	 */
	public SHE(){
		blockSize = 32;
		blockNo = 0;
		cfg = false;
		try {
			mD = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		buffer = ByteBuffer.allocate(blockSize);
	}
	
	/**
	 * Initializes the cipher with key, creates a random IV to use with the cipher.
	 * @param key A 256 bit key to encrypt with.
	 * @return A 256 bit IV that has been created for this cipher to use.
	 */
	public byte[] init(byte[] key){
		byte[] iv = new byte[blockSize];
		new SecureRandom().nextBytes(iv);
		init(iv, key);
		return iv;
	}
	
	/**
	 * Initializes the cipher with key and IV
	 * @param IV A 256 bit initialization vector to use for the cipher.
	 * @param key A 256 bit key to encrypt with.
	 */
	public void init(byte[] IV, byte[] key){
		if(IV.length != blockSize || key.length != blockSize)
			throw new RuntimeException("key and IV need to be same as block size (" + blockSize + ")."); //TODO subclass exception
		this.key = key;
		this.IV = IV;
		cfg = true;
		blockNo = 0;
		buffer.clear();// = new byte[blockSize];
		//bufferIndex = 0;
	}
	
	private byte[] cryptBlock(){
		byte[] iv = Arrays.copyOf(IV, IV.length);// + BLOCKSIZE);
		//System.arraycopy(IV, 0, iv, 0, BLOCKSIZE);
		iv[blockNo % blockSize] += blockNo + 1;
		//iv = Misc.cleanXOR(iv, key); //for some reason this line runs much faster than the following two lines
		//Misc.XORintoA(iv, key);
		iv = Arrays.copyOf(iv, blockSize + iv.length);
		//Misc.arraycopy(key, 0, iv, BLOCKSIZE, BLOCKSIZE);
		System.arraycopy(key, 0, iv, blockSize, blockSize);
		buffer.flip();
		//System.out.println(Misc.bytesToHex(buffer.array()) + " " + buffer);
		byte[] toCrypt = new byte[blockSize];
		buffer.get(toCrypt, 0, buffer.remaining());
		buffer.clear();
		return Misc.cleanXOR(toCrypt, mD.digest(iv));
	}
	
	public boolean isConfigured(){
		return cfg;
	}
	
//	public byte[] updateWithInterrupts(byte[] input){
//		if(!cfg){
//			throw new RuntimeException("Cipher not configured.");
//		}
//		int numBlocks = (int) Math.floor((input.length + bufferIndex)/BLOCKSIZE);
//		byte[] out = new byte[numBlocks*BLOCKSIZE];
//		
//		for(int i=0; i < input.length; i++){
//			try{
//				buffer[bufferIndex++] = input[i];
//			}catch(IndexOutOfBoundsException e){
//				bufferIndex = 0;
//				i--;
//				//System.out.println(Misc.bytesToHex(buffer));
//				System.arraycopy(cryptBlock(), 0, out, blockNo*BLOCKSIZE, BLOCKSIZE);
//				blockNo++;
//				buffer = new byte[BLOCKSIZE];
//			}
//		}
//		if(bufferIndex == 32){
//			bufferIndex = 0;
//			System.arraycopy(cryptBlock(), 0, out, blockNo*BLOCKSIZE, BLOCKSIZE);
//			buffer = new byte[BLOCKSIZE];
//		}
//		//System.out.println(bufferIndex);
//		//System.out.println(Misc.bytesToHex(out));
//		return out;
//	}
	
	/**
	 * Continues a multiple-part encryption or decryption operation (depending on how this cipher was initialized), processing another data part.
	 * The bytes in the input buffer are processed, and the result is stored in a new buffer.
	 *
	 * If input has a length of zero, this method returns null.
	 * @param input
	 * @return
	 */
	public byte[] update(byte[] input){
		if(!cfg){
			throw new RuntimeException("Cipher not configured.");//TODO
		}
		if(input.length == 0)
			return new byte[]{};//null;
		if(buffer.position() != 0){
			int bufferRemain = buffer.remaining();
			byte[] in2 = new byte[input.length + buffer.remaining()];//Arrays.copyOf(buffer., input.length + buffer.remaining());//new byte[input.length + bufferIndex];
			buffer.get(in2, 0, buffer.remaining());
			//System.out.println(Misc.bytesToHex(in2));
			//System.arraycopy(buffer, 0, in2, 0, bufferIndex);
			System.arraycopy(input, 0, in2, bufferRemain, input.length);
			input = in2;
		}
		
		int numBlocks = (int) Math.floor(input.length/blockSize);
		//System.out.println(numBlocks);
		byte[] out = new byte[blockSize * numBlocks];
		for(int i = 0; i < numBlocks; i++){
			buffer.clear();
			//System.out.println(buffer.remaining());
			buffer.put(input, blockSize*i, blockSize);//System.arraycopy(input, blockSize*i, buffer, 0, blockSize);
			System.arraycopy(cryptBlock(), 0, out, i * blockSize, blockSize);//TODO do encryption on buffer
			blockNo++;
		}
		buffer.clear();// = new byte[blockSize];
		if(!(input.length % blockSize == 0)){
			buffer.put(input, numBlocks*blockSize, input.length - numBlocks*blockSize);
			//System.arraycopy(input, numBlocks*blockSize, buffer, 0, input.length - numBlocks*blockSize);
			//bufferIndex = input.length - numBlocks*blockSize;
		}
		//System.out.println(Misc.bytesToHex(out));
		return out;
	}
//	public byte[] doFinalWithInterrupts(byte[] input){
//		byte[] main = updateWithInterrupts(input);
//		byte[] out;
//		//if buffer isn't empty add a padding indicator to the end of data
//		if(bufferIndex != 0){
//			
//			buffer[bufferIndex] = 0x69;
//			bufferIndex++;
//			//System.out.println(Misc.bytesToHex(buffer));
//			buffer = cryptBlock();
//			//add buffer to end of main
//			out = new byte[main.length + buffer.length];
//			System.arraycopy(main, 0, out, 0, main.length);
//			System.arraycopy(buffer, 0, out, main.length, buffer.length);
//		}else{
//			//remove padding
//			int endIndex = main.length-1 ;
//			while(main[endIndex] == 0 || main[endIndex] == 0x69){
//				endIndex --;
//				if(main[endIndex] == 0x69){
//					endIndex--;
//					break;
//				}
//			}
//			//System.out.println("endindex " + endIndex);
//			out = new byte[endIndex + 1];
//			System.arraycopy(main, 0, out, 0, endIndex+1);
//		}
//				
//		blockNo = 0;
//		IV = null;
//		key = null;
//		cfg = false;
//		bufferIndex = 0;
//		
//		return out;
//	}
	/**
	 * Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation.
	 * The bytes in the input buffer, and any input bytes that may have been buffered during a previous update operation, are processed, with padding (if requested) being applied. 
	 *
	 * Upon finishing, this method resets this cipher object to the state it was in before initialized via a call to init. That is, the object is reset and needs to be re-initialized before it is available to encrypt or decrypt more data.
	 * @param input the input buffer
	 * @return the new buffer with the result
	 */
	public byte[] doFinal(){
		return doFinal(new byte[]{});
	}
	
	/**
	 * Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation.
	 * The bytes in the input buffer, and any input bytes that may have been buffered during a previous update operation, are processed, with padding (if requested) being applied. 
	 *
	 * Upon finishing, this method resets this cipher object to the state it was in before initialized via a call to init. That is, the object is reset and needs to be re-initialized before it is available to encrypt or decrypt more data.
	 * @param input the input buffer
	 * @return the new buffer with the result
	 */
	public byte[] doFinal(byte[] input){
		byte[] main = update(input);
		byte[] out;
		//if buffer isn't empty add a padding indicator to the end of data
		if(buffer.position() != 0){
			buffer.put((byte) 0x69);//buffer[bufferIndex] = 0x69;
			//bufferIndex++;
			//System.out.println(Misc.bytesToHex(buffer));
			buffer.clear();
			buffer.put(cryptBlock());
			//add buffer to end of main
			out = new byte[main.length + buffer.remaining()];
			System.arraycopy(main, 0, out, 0, main.length);
			int bufferRemain = buffer.remaining();
			buffer.get(out, main.length, bufferRemain);
			//System.arraycopy(buffer, 0, out, main.length, buffer.length);
		}else{
			//remove padding
			int endIndex = main.length-1 ;
			while(main[endIndex] == 0 || main[endIndex] == 0x69){
				endIndex --;
				if(main[endIndex] == 0x69){
					endIndex--;
					break;
				}
			}
			//System.out.println("endindex " + endIndex);
			out = new byte[endIndex + 1];
			System.arraycopy(main, 0, out, 0, endIndex+1);
		}
				
		blockNo = 0;
		IV = null;
		key = null;
		cfg = false;
		buffer.clear();//bufferIndex = 0;
		
		return out;
	}
	
	public static class EncryptedOutputStream extends FilterOutputStream{
		private SHE cipher;
		/**
		 * If true, when this stream is closed using its close() method, any 
		 * unprocessed data will be padded and then encrypted.
		 * If false, when this stream is closed any unprocessed data will be discarded.
		 */
		public boolean padOnClose;
		
		/**
		 * Constructs an EncryptedOutputStream from an OutputStream and a Cipher. 
		 * Note: if the specified output stream or cipher is null, a NullPointerException may be thrown later when they are used.
		 * @param out the OutputStream object
		 * @param cipher an initialized Cipher object
		 */
		public EncryptedOutputStream(OutputStream out, SHE cipher) {
			super(out);
			if(!cipher.isConfigured())
				throw new RuntimeException("Cipher not configured.");
			padOnClose = true;
			this.cipher = cipher;
		}
		/**
		 * Writes the specified byte to this output stream.
		 */
		@Override
		public void write(int b) throws IOException{
			out.write(cipher.update(new byte[]{(byte) b}));
		}
		
		
		@Override
		public void write(byte[] b) throws IOException{
			//byte[] debug;// = new byte[b.length];
			//debug = ;
			//System.out.print(Misc.bytesToHex(debug));
			out.write(cipher.update(b));
			//return debug;
		}
		
		public void write(byte[] b, int off, int len) throws IOException{
			byte[] todo = new byte[len];
			System.arraycopy(b, off, todo, 0, len);
			write(todo);
		}
		
		//@Override
	//	public void flush() throws IOException{
			//write(cipher.doFinal());
			//out.flush();
		//}
		@Override
		public void close() throws IOException{
			if(padOnClose)
				out.write(cipher.doFinal());
			out.close();
		}
		/**
		 * 
		 * @return the underlying SHE cipher for this Stream
		 */
		public SHE getCipher(){
			return cipher;
		}
		
	}
	
	public static class EncryptedInputStream extends FilterInputStream{
		private SHE cipher;
		private ByteBuffer buffer;
		
		public EncryptedInputStream(InputStream in, SHE cipher) {
			super(in);
			if(!cipher.isConfigured())
				throw new RuntimeException("Cipher not configured.");
			this.cipher = cipher;
			buffer = ByteBuffer.allocate(cipher.blockSize*2);
		}
		
		private void bufferBlock() throws IOException{
			byte[] plainText = new byte[cipher.blockSize];
			in.read(plainText);
			buffer.put(cipher.update(plainText));
		}
		
		public int read() throws IOException{
			if(buffer.hasRemaining())
				return buffer.get();
			else{
				bufferBlock();
				return buffer.get();
			}
		}
		
		public int read(byte[] b) throws IOException{
			
		}
		
		public int read(byte[]b, int off, int len) throws IOException{
			byte[] ret = new byte[len-off];
			if(in.read(ret, off, len) == -1)
				return -1;
			ret = cipher.update(ret);
			if(ret.length > len-off){
				
			}
		}
		
	}
	
	public static class EncryptionResult implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6451163680434801851L;
		private byte[] text;
		private byte[] iv;
		/**
		 * @param text
		 * @param iv
		 */
		public EncryptionResult(byte[] iv, byte[] text) {
			//super();
			this.text = text;
			this.iv = iv;
		}
		
		public EncryptionResult(byte[] simpleSerial){
			int ivLength = simpleSerial[0];
			int outputLength = simpleSerial.length - ivLength -1;
			iv = new byte[ivLength];
			text = new byte[outputLength];
			System.arraycopy(simpleSerial, 1, iv, 0, ivLength);
			System.arraycopy(simpleSerial, ivLength + 1, text, 0, outputLength);
		}
		
		/**
		 * needs testing
		 * @return the encryption result as a byte array in the form (ivLength|iv|ciphertext) 
		 */
		public byte[] simpleSerial(){
			byte[] out = new byte[text.length + iv.length + 1];
			out[0] = (byte) iv.length;
			System.arraycopy(iv, 0, out, 1, iv.length);
			System.arraycopy(text, 0, out, iv.length + 1, text.length);
			return out;
		}
		
		/**
		 * @return the cipherText
		 */
		public byte[] getText() {
			return text;
		}
		
		/**
		 * @return the iv
		 */
		public byte[] getIv() {
			return iv;
		}
		
		@Override
		public String toString() {
			return "EncryptionResult [iv="
					+ Misc.bytesToHex(iv) + "[text=" + Misc.bytesToHex(text)+ "]\n" + Misc.bytesToHex(simpleSerial());
		}
	}
	public class Algorithm{
		public static final String SHA1 = "SHA-1";
		public static final String SHA256 = "SHA-256";
		public static final String SHA384 = "SHA-384";
		public static final String SHA512 = "SHA-512";
		
		private String algo;
		public Algorithm(String algorithm){
			if(!algorithm.equals(SHA1) || !algorithm.equals(SHA256) || 
					!algorithm.equals(SHA384) || !algorithm.equals(SHA512))
				throw new RuntimeException("Invalid algorithm " + algorithm);
			algo = algorithm;
		}
		public String getAlgorithm() {
			return algo;
		}
		public void setAlgorithm(String algo) {
			if(!algo.equals(SHA1) || !algo.equals(SHA256) || 
					!algo.equals(SHA384) || !algo.equals(SHA512))
				throw new RuntimeException("Invalid algorithm " + algo);
			this.algo = algo;
		}
		
	}
}
