package blackdoor.util;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class SHECipher {
	private int blockSize = 32;
	private SecureRandom random;
	private byte[] key;
	private boolean initd;
	private Queue<Byte> buffer;
	private int block;
	private int opmode;
	private byte[] IV;
	public static final int DECRYPT_MODE = 2;
	public static final int ENCRYPT_MODE = 1;
	private static final int pad = 69;

	SHECipher(){
		initd = false;
	}
	public void setBlockSize(int blockSize) throws IllegalBlockSizeException{
		if(blockSize == 32 || blockSize == 64){
			this.blockSize = blockSize;
		}else throw new IllegalBlockSizeException();
	}
	public String getAlgorithm(){
		return "SHE";
	}
	public final int getBlockSize(){
		return blockSize;
	}
	public byte[] getIV(){
		return IV;
	}
	public void setIV(byte[] IV)throws IllegalBlockSizeException{
		if(IV.length < 32)
			throw new IllegalBlockSizeException("IV too short.");
		else this.IV = IV;
	}
	public final void init(int opmode, byte[] key) throws InvalidKeyException{
		init(opmode, key, new SecureRandom());
	}
	public final void init(int opmode, byte[] key, SecureRandom random) throws InvalidKeyException{
		if(opmode > 2)
			throw new InvalidKeyException("Invalid opmode.");
		if(key.length < 32)
			throw new InvalidKeyException("Key too short.");
		this.opmode = opmode;
		this.key = key;
		this.random = random;
		if(IV == null){
			IV = new byte[32];
			random.nextBytes(IV);
		}
		block = 0;
		initd = true;
	}
	public final byte[] update(byte[] input){
		if(initd){
			buffer.
//			// cant do this buffer = null;
//			byte[] toDoBuffer = new byte[blockSize];
//			byte[] outputBuffer = new byte[input.length];
//			int index = 0;
//			while(index < input.length - blockSize - 1){
//				System.arraycopy(input, index, toDoBuffer, 0, blockSize);
//				//do crypto
//				block ++;
//				index += blockSize;
//			}
//			if(input.length % blockSize != 0){
//				buffer = Arrays.copyOfRange(input, index, input.length-1);
//			}
//			return outputBuffer;
		}else throw new IllegalStateException("Not Initialized.");
	}
	public final byte[] doFinal() throws IllegalBlockSizeException, BadPaddingException{
		if(buffer!=null)
			return doFinal(buffer);
		block = 0;
		byte[] fin = new byte[blockSize];
		fin[0] = pad;
		return fin;
	}
	public final byte[] doFinal(byte[] input) throws IllegalBlockSizeException,BadPaddingException{
		byte[] outputBuffer = new byte[input.length + buffer.length];
		
		block = 0;
	}
	               
}
