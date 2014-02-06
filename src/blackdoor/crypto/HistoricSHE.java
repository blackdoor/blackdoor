/**
 * 
 */
package blackdoor.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import blackdoor.util.Crypto.EncryptionResult;

import javax.xml.bind.DatatypeConverter;

//import portunes.util.Crypto.EncryptionResult;

/**
 * @author kAG0
 * a class containing simple static implementations of secure but non-standardized encryption algorithms
 * just SHE for now actually
 * 
 * Recommended usage: 	large inputs | stream cipher: create an instance of SHE and pass blocks to calculateSHEBlock, pass the final block to claculateFinalSHEBlock.
 * 						small inputs: just call doSHE
 * 
 */
@Deprecated //this is the old SHE.java, replaced with what was CleanSHE.java. This class still works but has too many methods and is messy.
public class HistoricSHE {
	public static final int blockSize = 32;
	private int blockNo;
	private byte[] IV;
	private byte[] key;
	private boolean configured;


	/**
	 * deprecated, use default constructor and configure() instead.
	 * @param key
	 * @param IV
	 */
	@Deprecated
	HistoricSHE(byte[] key, byte[] IV){
		if(IV.length == blockSize)
			this.IV = IV;
		else
			throw new RuntimeException("invalid IV length");
		if(key.length == blockSize)
			this.IV = key;
		else
			throw new RuntimeException("invalid key length");
		blockNo = 0;
	}
	HistoricSHE(){
		configured = false;
		blockNo = 0;
	}

	public void configure(byte[] key, byte[] IV){
		if(IV.length == blockSize)
			this.IV = IV;
		else
			throw new RuntimeException("invalid IV length");
		if(key.length == blockSize)
			this.IV = key;
		else
			throw new RuntimeException("invalid key length");
		blockNo = 0;
		configured = true;
	}

	public void setIV(byte[] iV) {
		IV = iV;
	}
	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] calculateSHEBlock(byte[] text){
		byte[] otp = null;
		if(configured){
			otp = Arrays.copyOf(IV, blockSize);
			otp[blockNo%blockSize] += blockNo+1;
			Misc.XORintoA(otp, key);
			otp = Hash.getSHA256(otp);
			Misc.XORintoA(otp, text);
		}else
			throw new RuntimeException("Instance not configured, call configure(byte[], byte[])");
		return otp;
	}

	/**
	 * note: resets instance to the condition it was in after configure was called.
	 * @param text
	 * @return the last block of calculated text
	 */
	public byte[] calculateFinalSHEBlock(byte[] text){
		byte[] otp = null;
		if(configured){
			int length = text.length;
			otp = Arrays.copyOf(IV, blockSize);
			otp[blockNo%blockSize] += blockNo+1;
			Misc.XORintoA(otp, key);
			otp = Hash.getSHA256(otp);

			//pad text
			if(length != blockSize){
				text = Arrays.copyOf(text, blockSize);
				text[length] = (byte) 0xFF;
			}

			Misc.XORintoA(otp, text);

			//remove padding
			int endIndex = otp.length -1 ;
			if(otp[endIndex] == (byte) 0xFF){
				endIndex--;
			}else{
				while(otp[endIndex] == 0){
					endIndex --;
					if(otp[endIndex] ==(byte) 0xFF){
						endIndex--;
						break;
					}
				}
			}
			
			if(endIndex+1 != otp.length){
				return Arrays.copyOf(otp, endIndex+1);
			}
		}else
			throw new RuntimeException("Instance not configured, call configure(byte[], byte[])");
		blockNo = 0;
		return otp;
	}


	/**
	 * Calculate a single block with SimpleHashEncryption (see doSHE for details)
	 * @param block The block number, text and initialization vector for the block that is to be calculated
	 * @param key a 128 bit or stronger key
	 * @return an array containing the calculated block
	 */
	public static byte[] doSHEBlock(Block block, byte[] key){
		//make sure key is at least 128 bits.
		if(key.length < 16)
			throw new RuntimeException("Key too short");
		//make sure iv is at least 256 bits.
		if(block.getIV().length < blockSize)
			throw new RuntimeException("IV too short.");
		//make sure text block is exactly 256 bits.
		if(block.getText().length != blockSize)
			throw new RuntimeException("Block is not 256 bits.");

		//create a copy of the iv so we can increment it
		byte[] iv = new byte [blockSize];
		System.arraycopy(block.getIV(), 0, iv, 0, iv.length);

		//increment the iv based on the block number
		iv[block.getBlockNo()%blockSize] += block.getBlockNo()+1;

		return Misc.XOR(block.getText(), //xor the below with the text
				Hash.getSHA256( //hash the key salted iv
						Misc.XOR(key, iv))); // salt the iv with the key
	}



	/*
	 * a version of the block calculation that modifies text and has no return, more memory efficient.
	 * however key, text and iv all need to be 256 bits
	 */
	public static void doSHEBlock(byte[] text, int blockNo, byte[] IV, byte[] key){
		if(text.length != blockSize || IV.length != blockSize || key.length !=blockSize)
			throw new RuntimeException("invalid input length.");
		byte[] otp = Arrays.copyOf(IV, IV.length);
		otp[blockNo%blockSize] += blockNo+1;
		Misc.XORintoA(otp, key);
		otp = Hash.getSHA256(otp);
		Misc.XORintoA(text, otp);
	}
	/**
	 * Calculate a single block with SimpleHashEncryption (see doSHE for details). 
	 * Use doSHEBlock if the parameters need to be checked for size, null, etc.
	 * @param block The block number, text and initialization vector for the block that is to be calculated
	 * @param key a 128 bit or stronger key
	 * @return an array containing the calculated block
	 */
	private static byte[] getSHEBlock(Block block, byte[] key){
		//create a copy of the iv so we can increment it
		byte[] iv = new byte [blockSize];
		System.arraycopy(block.getIV(), 0, iv, 0, iv.length);

		//increment the iv based on the block number
		iv[block.getBlockNo()%blockSize] += block.getBlockNo()+1;

		return Misc.XOR(block.getText(), //xor the below with the text
				Hash.getSHA256( //hash the key salted iv
						Misc.XOR(key, iv))); // salt the iv with the key
	}

	/**
	 * Simple/Secure Hash Encryption encryption/decryption method which uses the 
	 * identical nature of the encryption and decryption algorithms in the 
	 * counter (CTR) mode of operation for block ciphers to use a one way hash 
	 * function instead of a typical encryption algorithm. SHE uses SHA256 with 
	 * a 256bit block size, and a key of at least 126 bits and a generated IV of 
	 * 256 bits. This method operates as a block cipher, 
	 * @param input The text to calculate, must be less that 2GB or 2^32 bytes, due to array restrictions in java.
	 * @param key The key to use for encryption/decryption, must be at least 128 bits.
	 * @return An EncryptionResult containing the iv and calculated text.
	 */
	public static  EncryptionResult doSHE(byte[] input, byte[] key){
		return doSHE(input, key, null);
	}
	/**
	 * Simple/Secure Hash Encryption encryption/decryption method which uses the 
	 * identical nature of the encryption and decryption algorithms in the 
	 * counter (CTR) mode of operation for block ciphers to use a one way hash 
	 * function instead of a typical encryption algorithm. SHE uses SHA256 with 
	 * a 256bit block size, and a key of at least 126 bits and an IV of at least 
	 * 256 bits. This method operates as a block cipher, 
	 * @param input The text to calculate, must be less that 2GB or 2^32 bytes, due to array restrictions in java.
	 * @param key The key to use for encryption/decryption, must be at least 128 bits.
	 * @param IV The initializaiton vector to use, must be at least 256 bits.
	 * @return An EncryptionResult containing the iv and calculated text.
	 */
	public static  EncryptionResult doSHE(byte[] input, byte[] key, byte[] IV){
		//List inputList;
		//pad input length to a multiple of 32
		//input[0] = 99;
		if(input.length%blockSize != 0){
			int length = input.length;
			//Misc.PrintMemInfo(Runtime.getRuntime());
			//input = Arrays.copyOf(input, (input.length/32+1)*32);
			byte[] temp = null; 
			//System.out.println((input.length/blockSize+1)*blockSize / (1024*1024));
			temp=new byte[(input.length/blockSize+1)*blockSize];
			System.arraycopy(input, 0, temp, 0, input.length);
			input = temp;
			temp = null;
			input[length] = 69;
			//Misc.PrintMemInfo(Runtime.getRuntime());//System.gc();
		}
		int numBlocks = input.length/blockSize;

		//make sure key is at least 128 bits
		if(key.length < 16)
			throw new RuntimeException("Key too short");

		// if initialization vecor is null, get a new CSRN for it
		if(IV == null){
			SecureRandom random = new SecureRandom();
			IV = new byte[blockSize];
			random.nextBytes(IV);
		}else{ //if iv is not null make sure it is long enough
			if(IV.length < blockSize)
				throw new RuntimeException("IV too short."); //throw an error
		}
		byte[] tmp = new byte[blockSize];
		Block block = new Block(0, null, IV);

		//loop through each block
		for(int i = 0; i < numBlocks; i++){
			//copy block into temp array
			System.arraycopy(input, i*blockSize, tmp, 0, blockSize);
			//copy encrypted block back into input
			block.setBlockNo(i);
			block.setText(tmp);
			System.arraycopy(getSHEBlock(block, key), 0, input, i*blockSize, blockSize);

		}
		tmp = null;
		block = null;
		System.gc();
		//trim any null bytes from end of array
		int endIndex = input.length -1 ;
		while(input[endIndex] == 0){
			endIndex --;
			if(input[endIndex] == 69){
				endIndex--;
				break;
			}

		}
		//byte out[] = new byte[endIndex+1];
		//System.arraycopy(input, 0, out, 0, endIndex+1);	
		if(endIndex+1 != input.length){
			return new EncryptionResult(Arrays.copyOf(input, endIndex+1), IV, null);
		}else
			return new EncryptionResult(input, IV, null);
	}


	public static class Block{
		public int blockNo;
		public byte[] text;
		public byte[] IV;
		/**
		 * @param blockNo
		 * @param text
		 * @param iV
		 */
		public Block(int blockNo, byte[] text, byte[] iV) {
			super();
			this.blockNo = blockNo;
			this.text = text;
			IV = iV;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "SHEBlock [blockNo=" + blockNo + ", text="
					+ Arrays.toString(text) + ", IV=" + Arrays.toString(IV)
					+ "]";
		}
		/**
		 * @return the text
		 */
		public byte[] getText() {
			return text;
		}
		/**
		 * @param text the text to set
		 */
		public void setText(byte[] text) {
			this.text = text;
		}
		/**
		 * @return the iV
		 */
		public byte[] getIV() {
			return IV;
		}
		/**
		 * @param iV the iV to set
		 */
		public void setIV(byte[] iV) {
			IV = iV;
		}
		/**
		 * @return the blockNo
		 */
		public int getBlockNo() {
			return blockNo;
		}
		/**
		 * @param blockNo the blockNo to set
		 */
		public void setBlockNo(int blockNo) {
			this.blockNo = blockNo;
		}

	}
}
