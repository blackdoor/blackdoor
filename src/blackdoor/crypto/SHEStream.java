/**
 * 
 */
package blackdoor.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import blackdoor.struct.ByteQueue;
import blackdoor.util.Misc;

/**
 * @author nfischer3
 *
 */
public class SHEStream {
	protected int blockSize;
	protected int keySize;
	private int blockNo = 0;
	private boolean cfg = false;
	private byte[] key;
	private ByteQueue buffer;
	private MessageDigest mD;
	private byte[] prehash;
	
	public SHEStream(){
		blockSize = 64;
		//keySize = 32;
		try {
			mD = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] init(byte[] key){
		keySize = key.length;
		byte[] iv = new byte[keySize];
		new SecureRandom().nextBytes(iv);
		init(iv, key);
		return iv;
	}
	
	public void init(byte[] IV, byte[] key) {
		if(IV.length != key.length)
			throw new RuntimeException("Key and IV size must be equal.");
		keySize = key.length;
		this.key = key;
		prehash = Misc.cleanXOR(IV, key);
		blockNo = 0;
		buffer = new ByteQueue(blockSize*2);
		buffer.setResizable(true);
		cfg = true;
	}
	
	public byte[] crypt(byte[] text){
		if(!cfg)
			throw new RuntimeException("Cipher not initialized.");
		if(text.length > buffer.capacity())
			buffer.resize(text.length + blockSize);
		while(buffer.filled() < text.length){
			bufferKeystream();
		}
		//System.out.println(buffer);
		//System.out.println(text.length);
		return Misc.XORintoA(buffer.deQueue(text.length), text);
	}
	
	protected void bufferKeystream(){
		int i = blockNo % keySize;
		int inc = (blockNo/keySize) + 1;
		prehash[i] ^= key[i];					// expose IV[i] in prehash
		prehash[i] += inc;	// apply ctr
		prehash[i] ^= key[i];					// cover IV[i] in prehash with key[i]
		buffer.enQueue(mD.digest(prehash));		// buffer keystream
		prehash[i] ^= key[i];					// expose IV[i[ in prehash
		prehash[i] -= inc;	// remove ctr
		prehash[i] ^= key[i];					// cover IV[i[ in prehash with key[i]
		blockNo++;
	}

}
