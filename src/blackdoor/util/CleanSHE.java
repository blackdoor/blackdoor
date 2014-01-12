/**
 * 
 */
package blackdoor.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author nfischer3
 * uses SHE256 v1.1 (H(key || incremented IV) instead of H(key XOR incremented IV))
 */
public class CleanSHE {
	
	public static final int BLOCKSIZE = 32;
	private int blockNo;
	private byte[] IV;
	private byte[] key;
	private boolean cfg;
	private byte[] buffer = new byte[BLOCKSIZE];
	private int bufferIndex; //index at which to place next byte in buffer
	private MessageDigest mD;
	
	//parameters for different algo and block sizes will go here
	CleanSHE(){
		blockNo = 0;
		cfg = false;
		try {
			mD = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init(byte[] key){
		byte[] iv = new byte[BLOCKSIZE];
		new SecureRandom().nextBytes(IV);
		init(iv, key);
	}
	
	public void init(byte[] IV, byte[] key){
		if(IV.length != BLOCKSIZE || key.length != BLOCKSIZE)
			throw new RuntimeException("key and IV need to be same as block size (" + BLOCKSIZE + ")."); //TODO subclass exception
		this.key = key;
		this.IV = IV;
		cfg = true;
		blockNo = 0;
		buffer = new byte[BLOCKSIZE];
	}
	
	//returns the block in buffer encrypted
	private byte[] cryptBlock(){
		byte[] iv = Arrays.copyOf(IV, IV.length);
		iv[blockNo % BLOCKSIZE] += blockNo + 1;
		iv = Arrays.copyOf(iv, BLOCKSIZE + iv.length);
		System.arraycopy(key, 0, iv, BLOCKSIZE, BLOCKSIZE);
		return Misc.cleanXOR(buffer, mD.digest(iv));
	}
	
	public byte[] update(byte[] input){
		if(bufferIndex != 0){
			byte[] in2 = Arrays.copyOf(buffer, input.length + bufferIndex);//new byte[input.length + bufferIndex];
			//System.out.println(Misc.bytesToHex(in2));
			//System.arraycopy(buffer, 0, in2, 0, bufferIndex);
			System.arraycopy(input, 0, in2, bufferIndex, input.length);
			input = in2;
		}
		
		int numBlocks = (int) Math.floor(input.length/BLOCKSIZE);
		//System.out.println(numBlocks);
		byte[] out = new byte[BLOCKSIZE * numBlocks];
		for(int i = 0; i < numBlocks; i++){
			//System.out.println("i:"+i+" block:" + blockNo);
			System.arraycopy(input, BLOCKSIZE*i, buffer, 0, BLOCKSIZE);
			System.arraycopy(cryptBlock(), 0, out, i * BLOCKSIZE, BLOCKSIZE);//TODO do encryption on buffer
			blockNo++;
		}
		buffer = new byte[BLOCKSIZE];
		if(input.length % BLOCKSIZE == 0){
			
			bufferIndex = 0;
		}else{
			//buffer = new byte[BLOCKSIZE];
			System.arraycopy(input, numBlocks*BLOCKSIZE, buffer, 0, input.length - numBlocks*BLOCKSIZE);
			bufferIndex = input.length - numBlocks*BLOCKSIZE;
		}
		//System.out.println(Misc.bytesToHex(out));
		return out;
	}
	
	public byte[] doFinal(byte[] input){
		byte[] main = update(input);
		byte[] out;
		//if buffer isn't empty add a padding indicator to the end of data
		if(bufferIndex != 0){
			buffer[bufferIndex] = 0x69;
			bufferIndex++;
			//System.out.println(Misc.bytesToHex(buffer));
			buffer = cryptBlock();
			//add buffer to end of main
			out = new byte[main.length + buffer.length];
			System.arraycopy(main, 0, out, 0, main.length);
			System.arraycopy(buffer, 0, out, main.length, buffer.length);
		}else{
			//remove padding
			int endIndex = main.length-1 ;
			while(main[endIndex] == 0){
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
		bufferIndex = 0;
		
		return out;
	}

}
