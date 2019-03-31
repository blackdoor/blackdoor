/**
 * 
 */
package black.door.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import black.door.util.Misc;
import black.door.struct.ByteQueue;

/**
 * @author nfischer3
 * Secure hash encryption, uses hash algorithms in CTR mode for mirrored, symmetric encryption.
 */
public class SHECipher implements Cipher{
	public static final int MIN_KEY_SIZE = 8;
	
	/**
	 * 
	 * @return the default instance with a 256 bit block size
	 */
	public static SHECipher getDefaultInstance(){
		try {
			return new SHECipher(MessageDigest.getInstance("SHA-256"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private int blockNo = 0;
	private boolean cfg = false;
	private byte[] key;
	private ByteQueue buffer;
	private MessageDigest mD;
	private IvParameterSpec iv;
	private byte[] prehash;
	
	
	public SHECipher(MessageDigest mD){
		this.mD = mD;
	}
	
	public boolean isConfigured() {
		return cfg;
	}
	
	public String getAlgorithm(){
		return mD.getAlgorithm();
	}
	
	public byte[] getIV(){
		return iv.getIV();
	}

	/**
	 * Initializes the cipher with key, creates a random IV to use with the cipher.
	 * @param key A key to encrypt with. Key can be any length over MIN_KEY_SIZE but a key longer than the block size will run more slowly. 
	 * @return An IV that has been created for this cipher to use. IV will be the same length as the key.
	 * @throws InvalidKeyException 
	 */
	public IvParameterSpec init(SecretKey key) throws InvalidKeyException{
		byte[] iv = new byte[key.getEncoded().length];
		new SecureRandom().nextBytes(iv);
		IvParameterSpec ivSpec =  new IvParameterSpec(iv);
		init(key, ivSpec);
		return ivSpec;
	}
	
	/**
	 * Initializes the cipher with key and iv
	 * @param iv An initialization vector to use for the cipher.
	 * @param key A key to encrypt with.
	 * @throws InvalidKeyException 
	 */
	@Override
	public void init(Key key, IvParameterSpec iv) throws InvalidKeyException {
		if(!(key instanceof SecretKey))
			throw new InvalidKeyException();
		int ivLength = iv.getIV().length;
		if(key.getEncoded().length < MIN_KEY_SIZE || key.getEncoded().length < ivLength)
			throw new InvalidKeyException("Key must be longer than " + MIN_KEY_SIZE + " bytes and key must be longer than IV.");
		this.key = key.getEncoded();
		this.iv = iv;
		prehash = Misc.XORintoA(this.key.length == ivLength ? iv.getIV() : Arrays.copyOf(iv.getIV(), this.key.length), this.key);
		blockNo = 0;
		buffer = new ByteQueue(getBlockSize()*2);
		buffer.setResizable(true);
		cfg = true;
	}
	
	/**
	 * @return the minimum number of bytes buffered at a time for crypting.
	 */
	public int getBlockSize(){
		return mD.getDigestLength();
	}
	
	public void reset(){
		cfg = false;
		Arrays.fill(key, (byte) 0);
		Arrays.fill(prehash, (byte) 0x0);
		buffer = null;
		mD.reset();
	}
	
	/**
	 * Continues a multiple-part encryption or decryption operation (depending on how this cipher was initialized), processing another data part.
	 * The bytes in the input buffer are processed, and the result is stored in a new buffer.
	 *
	 * If input has a length of zero, this method returns null.
	 * @param input
	 * @return
	 */
	public byte[] update(byte[] input){
		if(!cfg)
			throw new Exceptions.CipherNotInitializedException();
		if(input.length == 0)
			return null;
		if(input.length > buffer.capacity())
			buffer.resize(input.length + getBlockSize());
		while(buffer.filled() < input.length){
			bufferKeystream();
		}
		return Misc.XORintoA(buffer.deQueue(input.length), input);
	}
	
	/**
	 * Zero's out memory where the key is stored.
	 * After calling this method init() needs to be called again.
	 *
	public void zeroKey(){
		for(int i = 0; i < key.length; i++){
			key[i] = 0x0;
		}
		for(int i = 0; i < prehash.length; i++){
			key[i] = 0x0;
		}
		cfg = false;
	}
	*/
	
	protected void bufferKeystream(){
		int i = blockNo % key.length;
		int inc = (blockNo/key.length) + 1;
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
