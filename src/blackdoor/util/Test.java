package blackdoor.util;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.swing.text.PlainDocument;
import javax.xml.bind.DatatypeConverter;

//import org.apache.commons.io.FileUtils;

import blackdoor.util.Crypto.EncryptionResult;
import blackdoor.util.Crypto.InvalidKeyLengthException;
import blackdoor.util.Watch.StopWatch;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//byte[] b = new byte[]{23, 57, 23, 0};
		//byte[] a = new byte[]{(byte) 0x1111, 12, 3,4};
		//System.out.println(a);
		//System.out.println(b);
		//byte[] c = Misc.XOR(a, b);
		//System.out.println(c);
		//EncryptionResult x = ExperimentalCrypto.getSHEBlock(, );
		//arrayTest(b);
		//testStuff();
		cryptoTest();
		
	}
	
	public static void cryptoTest(){
		byte[] IV = new byte[32];
		byte[] bigIV = new byte[64];
		byte[] plainText = new byte[100000000];
		//byte[] plainText2 = new byte[100000000];
		byte[] key = new byte[32];
		for(int i = 0; i < plainText.length; i++){
			plainText[i] = (byte) ((i/32) +1);
		}
		long total=0;
		double average;
		StopWatch time = new StopWatch(false);
		CleanSHE cipher = new CleanSHE();
		byte[] cipherText;
		//byte[] decryptedText;
		
		//cipher.init(IV, key);
		//cipherText = cipher.doFinal(plainText);
		//cipher.init(IV, key);
		//cipherText = cipher.doFinal(plainText);
		
		for(int i = 0; i < 2; i++){
			System.out.println("Start");
			time.mark();
			//Misc.cleanXOR(IV, key);
			//System.arraycopy(IV, 0, bigIV, 32, IV.length);
			cipher.init(IV, key);
			cipherText = cipher.doFinal(plainText);
			//SHE.doSHE(plainText, key, IV);
			//cipher.init(IV, key);
			//decryptedText = cipher.doFinal(cipherText);
			total+=time.checkNS();
			
		}
		System.out.println(total);
		average = total/2;
		System.out.println("Normal: " + average);
		System.out.println(100/(average/1000000000) + "MB/s");
//		for(int i = 0; i < 3; i++){
//			time.mark();
//			cipher.init(IV, key);
//			cipherText = cipher.doFinalWithInterrupts(plainText);
//			//cipher.init(IV, key);
//			//decryptedText = cipher.doFinalWithInterrupts(cipherText);
//			total+=time.checkNS();
//		}
//		System.out.println(total);
//		average = total/3;
//		System.out.println("interrupt: " + average);
		
		//System.out.println("plaintext: " + Misc.bytesToHex(plainText));
		
		
		
		
		//System.out.println("ciphertex: " + Misc.bytesToHex(cipherText));
		//cipher.init(IV, key);
		//byte[] decryptedText = cipher.doFinal(cipherText);
		//System.out.println("decrytext: " + Misc.bytesToHex(decryptedText));
	}
	
	public static void arrayTest(byte[] arr){
		for(int i = 0; i < arr.length; i++){
			arr[i] = (byte) ~ arr[i];
		}
	}
	

}
