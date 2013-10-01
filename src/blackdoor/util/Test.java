package blackdoor.util;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.swing.text.PlainDocument;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;

import blackdoor.util.Crypto.EncryptionResult;
import blackdoor.util.Crypto.InvalidKeyLengthException;

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
		testStuff();
	}
	public static void arrayTest(byte[] arr){
		for(int i = 0; i < arr.length; i++){
			arr[i] = (byte) ~ arr[i];
		}
	}
	
	public static void testStuff() throws IOException{
	int mb = 1024*1024;
	Runtime runtime = Runtime.getRuntime();
	byte iv[];
	iv =new byte[32];
	byte key[] = new byte[32];
	new SecureRandom().nextBytes(iv);
	new SecureRandom().nextBytes(key);
	byte[] plaintext;
	//plaintext = "BGIKWRG669CCE1KIE6JBX8U4DSVRHHDS3JIHOL4667khhjhjDBFEQ14GVOENKTJLHJ9YCBVD2N44KH8MOKTCMI7BP6QH3W4NOKU6FUSE1R033EN4SFWRU94B7NX59SUDVIKF2Q6NIKL9M7F2OSELU41HBWJOA6V8D4OSSCY97H13ATZVFBJ64AD6AFYZ7PQ0IYMSQHOPRR8L4TI0303M0PJQY4NX32DUPKF0AZEI8ISGPAZKHD0ZAB5F1P6J679M890R75LN520Z8KA6ISM3037GDPQPOQHA4R6CFORZJ8N8QIAPFJ71IC7SW0H4TINDTA68PRGVUQC198VRESC2ZHROLJ23FVVGMJGPQMGZGY8OJSYLW01W5GW4MV0RXSVVSQEIBPWXLNYDUJKWB74LH4".getBytes();
	plaintext = FileUtils.readFileToByteArray(new File("C:/Users/kAG0/Pictures/Photography/Florence Summer 2013/_DSC8401_stitch.psd"));
	//plaintext[plaintext.length-1] = 0x0;
	//plaintext[plaintext.length-2] = 0x0;
	System.out.println("Plaintext length " + plaintext.length + "\n" + DatatypeConverter.printHexBinary(Arrays.copyOfRange(plaintext, plaintext.length - 256, plaintext.length)));
	
	System.out.println("file loaded into memory.");
	Misc.PrintMemInfo(runtime);
	
	EncryptionResult out = ExperimentalCrypto.doSHE(plaintext, key, iv);
	//System.out.println(DatatypeConverter.printHexBinary(plaintext));
	plaintext = null;
	//System.gc();
	System.out.println("encryption done.");
	Misc.PrintMemInfo(runtime);
	
	System.out.println(DatatypeConverter.printHexBinary(Arrays.copyOfRange(out.getOutput(), out.getOutput().length - 256, out.getOutput().length)));
	out = ExperimentalCrypto.doSHE(out.getOutput(), key, out.getIv());
	System.out.println("decryption done.");
	Misc.PrintMemInfo(runtime);
	
	System.out.println(DatatypeConverter.printHexBinary(Arrays.copyOfRange(out.getOutput(), out.getOutput().length - 256, out.getOutput().length)));
	}
}
