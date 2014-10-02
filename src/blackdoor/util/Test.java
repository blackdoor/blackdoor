package blackdoor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.text.PlainDocument;
import javax.xml.bind.DatatypeConverter;

//import org.apache.commons.io.FileUtils;







import blackdoor.auth.AuthTicket;
import blackdoor.crypto.SHE;
import blackdoor.crypto.Hash;
import blackdoor.crypto.HistoricSHE;
import blackdoor.crypto.Crypto.EncryptionResult;
import blackdoor.crypto.Crypto.InvalidKeyLengthException;
import blackdoor.crypto.HistoricSHE.EncryptedInputStream;
//import blackdoor.crypto.SHEStream;
import blackdoor.struct.ByteQueue;
import blackdoor.util.CommandLineParser.Argument;
import blackdoor.util.CommandLineParser.DuplicateOptionException;
import blackdoor.util.CommandLineParser.InvalidFormatException;
import blackdoor.util.Watch.StopWatch;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws Exception {
		//byte[] b = new byte[]{23, 57, 23, 0};
		//byte[] a = new byte[]{(byte) 0x1111, 12, 3,4};
		//System.out.println(a);
		//System.out.println(b);
		//byte[] c = Misc.XOR(a, b);
		//System.out.println(c);
		//EncryptionResult x = ExperimentalCrypto.getSHEBlock(, );
		//arrayTest(b);
		//testStuff();
		//System.out.println(InetAddress.getLocalHost().getAddress().length);
		//fileHashTest();
		//ticketTest();
		//cryptoTest();

		//DBPTest();
		commandLineParserTest();
		
		//SHEStreamTest();
		//NISTBench();
		//cryptoTest();
		//bufferTest();
		//qTest();
		//cryptoStreamTest();
	}
	
	public static void commandLineParserTest() throws DuplicateOptionException, InvalidFormatException{
		CommandLineParser clp = new CommandLineParser();
		clp.addArguments(new String[]{ "--source, ?","-r,--readonly", "--file,+", "*, -fl, --flag, -h this is helptext"});
		Argument arg = new Argument().setLongOption("blarg").setOption("b").setRequiredArg(true).setTakesValue(true).setValueHint("garg");
		clp.addArgument(arg);
		clp.addArgument(new Argument().setOption("I").setLongOption("include-directories").setValueHint("number").setHelpText("comma-separated list of accepted extensions."));
		String test1[] = new String[] {"source.txt", "dest.txt", "-r", "--file", "out.txt", "-fl", "-b"};
		System.out.println(clp.getHelpText());
		System.out.println(clp.params);
		//System.out.println(clp.getParsedArgs(test1));
		System.out.println(clp.args);
		Map out = clp.parseArgs(test1);
		System.out.println(out.keySet());
		System.out.println(out.entrySet());
		
		
	}
	
	public static void DBPTest(){
		DBP.DEBUG = true;
		DBP.DEMO = true;
		DBP.DEV = true;
		DBP.ERROR = true;
		DBP.WARNING = true;
		DBP.LOG_ALL = true;
		DBP.toggleDebug();
		DBP.printdebugln("test line" + 5);
		DBP.printdemoln("demo");
		DBP.printdevln("dev");
		DBP.printerrorln("err");
		DBP.ERROR_AS_SYSTEM_ERROR = true;
		DBP.printerrorln("err as sys err");
		DBP.printwarningln("warning");
		DBP.printlogln("log");
	}
	
	public static void castTest(Object x){
		System.out.println(x);
	}
	
	public static void qTest(){
		ByteQueue.main();
	}
	public static void bufferTest(){
		byte[] plainText = new byte[]{0,1,2,3,4,5};//new byte[100];
		ByteBuffer b = ByteBuffer.allocate(20);
		//for(int i = 0; i < plainText.length; i++){
		//	plainText[i] = (byte) ((i/32) +1);
		//}
		System.out.println("new "+Misc.bytesToHex(b.array()));
		b.put(plainText);
		b.put(plainText);
		//for(int i = 0; i < plainText.length; i++){
		//	b.put(plainText[i]);
		//}
		System.out.println("filled " + Misc.bytesToHex(b.array()));
		System.out.println(b);
		byte[]g = new byte[3];
		b.get(g);
		System.out.println("get first 3 " +Misc.bytesToHex(g));
		System.out.println(b);
		b.flip();
		System.out.println("flipped " + Misc.bytesToHex(b.array()));
		System.out.println(b);
		b.get(g);
		System.out.println("get 3 after flip " + Misc.bytesToHex(g));
		System.out.println(b);
		//b.flip();
		b.put((byte) 0xff);
		//System.out.println(Misc.bytesToHex(g));
		System.out.println("put "+Misc.bytesToHex(b.array()));
		System.out.println(b);
		b.get(g);
		System.out.println("get 3" + Misc.bytesToHex(g));
		System.out.println(b);
		b.flip();
		System.out.println(b);
	}
	static byte[] key512 = new byte[64];
	static byte[] key256 = new byte[32];
	static byte[] key128 = new byte[16];
	static byte[] plainText = new byte[1024];
	static void NISTBench() throws Exception{
		
		final int NUM_TESTS = 128;
		final int NUM_ITERATIONS = 2048;
		StopWatch timer = new StopWatch(false);
		Double[] results = new Double[NUM_TESTS];
		
		for(int t = 0; t < NUM_TESTS; t++){
			/*	Setup	*/
			Object obj = NISTSetup();
			timer.mark();
			for(int i = 0; i < NUM_ITERATIONS; i++){
				/*	Test	*/
				NISTOperation(obj);//stream.crypt(plainText);
			}
			results[t] = timer.checkS();
		}
		double time = Statistics.mean(Statistics.discardOutliers(results, 3));
		System.out.println((plainText.length/1024) * NUM_ITERATIONS/1024/time + " M bytes/sec");
	}
	
	static Object NISTSetup() throws Exception{
		/*######################################################################
		 *####	SHEStream	####################################################
		 *######################################################################*/
		SHEStream stream = SHEStream.getInstance();
		stream.init(key128);
		return stream;
		/*######################################################################*/
		
		/*######################################################################
		 *####	AES			####################################################
		 *######################################################################*
		Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
        final SecretKeySpec secretKey = new SecretKeySpec(key256, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher;
		/*######################################################################*/
		
	}
	
	static void NISTOperation(Object obj)throws Exception{
		/*######################################################################
		 *####	SHEStream	####################################################
		 *######################################################################*/
		SHEStream stream = (SHEStream) obj;
		stream.crypt(plainText);
		/*######################################################################*/
		
		/*######################################################################
		 *####	AES			####################################################
		 *######################################################################*
		((Cipher) obj).doFinal(plainText);
		/*######################################################################*/
	}
	
	static void SHEStreamTest() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		SHE s = SHE.getInstance();
		s.init(new byte[5]);
		s.doFinal(new byte[300]);
		
		
		SHEStream stream = SHEStream.getInstance();
		byte[] IV = new byte[32];
		byte[] thing = new byte[16];
		byte[] plainText = new byte[1000];
		byte[] plainText2 = new byte[1048576];
		byte[] key = new byte[32];
		for(int i = 0; i < plainText.length; i++){
			plainText[i] = (byte) ((i/32) +1);
		}
		StopWatch timer = new StopWatch(true);
		
		
		 Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
         final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		timer.mark();
		cipher.doFinal(plainText2);
		System.out.println("AES " + timer.checkMS());
		
		stream.init(thing, thing);
		timer.mark();
		stream.crypt(plainText2);
		System.out.println("128 " + timer.checkMS());
		
		stream.init(IV, key);
		timer.mark();
		stream.crypt(plainText2);
		System.out.println("256 " + timer.checkMS());

		//byte result[] = stream.crypt(ciphertext);
		

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		
		timer.mark();
		//Arrays.copyOf(plainText2, plainText2.length);
		md.digest(plainText2);
		System.out.println(timer.checkMS());
		timer.mark();
		//Arrays.copyOf(plainText2, plainText2.length);
		md.digest(thing);
		System.out.println(timer.checkMS());
	}
	
	public static void cryptoStreamTest(){
		File file = new File("out.txt");
		FileOutputStream fos = null;
		HistoricSHE.EncryptedOutputStream eos = null;
		HistoricSHE cipher = new HistoricSHE();
		byte[] IV = new byte[32];
		byte[] plainText = new byte[1000];
		byte[] key = new byte[32];
		for(int i = 0; i < plainText.length; i++){
			plainText[i] = (byte) ((i/32) +1);
		}
		System.out.println(Misc.bytesToHex(plainText));
		cipher.init(IV, key);
		try {
			fos = new FileOutputStream(file);
			eos = new HistoricSHE.EncryptedOutputStream(fos, cipher);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] debug =null;
		try {
			//debug= cipher.update(plainText);
			//System.out.println(Misc.bytesToHex(debug));
			//fos.write(debug);
			eos.write(plainText, 0 ,40);
			eos.write(plainText, 40, plainText.length-40);
			eos.close();
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}

		cipher.init(IV, key);
		try {
			FileInputStream fis = new FileInputStream(file);
			EncryptedInputStream eis = new EncryptedInputStream(fis, cipher);
			byte[] readFile = new byte[(int) file.length()];

			eis.read(readFile);
			eis.close();
			System.out.println(Misc.bytesToHex(readFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void fileHashTest(){
		try {
			File file = new File("D:/Users/nfischer3/Videos/Escape Plan 2013 HDTV AC3 XViD - OLDTiMERS.avi");
			System.out.println(Misc.bytesToHex(Hash.getFileHash(file)));
			System.out.println(Misc.bytesToHex(Hash.getSHA256(Files.readAllBytes(file.toPath()))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ticketTest(){
		AuthTicket tic1 = null;
		try {
			tic1 = new AuthTicket("jim", 120, InetAddress.getLocalHost(), (byte) 6);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] pass = Hash.getSHA256("pass".getBytes());
		//System.out.println(Misc.bytesToHex(pass));
		byte[] ticket = tic1.generate(pass);
		//System.out.println(Misc.bytesToHex(ticket));
		HistoricSHE cipher = new HistoricSHE();
		HistoricSHE.EncryptionResult result = new HistoricSHE.EncryptionResult(ticket);
		cipher.init(result.getIv(), pass);
		System.out.println("Test: " + Misc.bytesToHex(cipher.doFinal(result.getText())));
		AuthTicket tic2 = new AuthTicket(pass, ticket);
		System.out.println(tic1);
		System.out.println(tic2);
	}
	
	
	public static void cryptoTest(){
		byte[] IV = new byte[32];
		byte[] bigIV = new byte[64];
		byte[] plainText = new byte[1000];
		byte[] plainText2 = new byte[1048576];
		byte[] key = new byte[32];
		for(int i = 0; i < plainText.length; i++){
			plainText[i] = (byte) ((i/32) +1);
		}
		
		
		double total=0;
		double average;
		StopWatch time = new StopWatch(false);
		HistoricSHE cipher = new HistoricSHE();
		byte[] cipherText;
		byte[] cipherTemp;
		byte[] cipherTemp2;
		//IV = cipher.init(key);
//		System.out.println("T1");
//		cipherTemp = cipher.update(Arrays.copyOfRange(plainText, 0, 72));
//		System.out.println("T2");
//		cipherTemp2 = cipher.doFinal(Arrays.copyOfRange(plainText, 72, plainText.length));
//		cipherText = new byte[cipherTemp.length + cipherTemp2.length];
//		System.arraycopy(cipherTemp, 0, cipherText, 0, cipherTemp.length);
//		System.arraycopy(cipherTemp2, 0, cipherText, cipherTemp.length,	cipherTemp2.length);
//		System.out.println(Misc.bytesToHex(cipherText));
//		SHE.EncryptionResult cipherResult = new SHE.EncryptionResult(IV, cipherText);
//		System.out.println(Misc.bytesToHex(cipherResult.simpleSerial()));
//		System.out.println(cipherResult);
//		//cipher.init(IV, key);
//		//System.out.println(Misc.bytesToHex(cipher.doFinal(cipherText)));
//		SHE.EncryptionResult cipherResult2 = new SHE.EncryptionResult(cipherResult.simpleSerial());
//		System.out.println(cipherResult2);
//		System.out.println(Misc.bytesToHex(cipherResult2.getText()));
//		cipher.init(cipherResult2.getIv(), key);
//		
//		System.out.println(Misc.bytesToHex(cipher.doFinal(cipherResult2.getText())));
		
		int tests = 1;
		for(int i = 0; i < tests; i++){
			cipher.init(IV, key);
			time.mark();
			cipherText = cipher.doFinal(plainText);
			total += time.checkS();
			System.out.println(Misc.bytesToHex(cipherText));
			cipher.init(IV, key);
			System.out.println(Misc.bytesToHex(cipher.doFinal(cipherText)));
			
		}
		average = tests/total;
		System.out.println(plainText.length/average/1000 + " kilobytes/sec");
		
//		for(int i = 0; i < 2; i++){
//			System.out.println("Start");
//			time.mark();
//			//Misc.cleanXOR(IV, key);
//			//System.arraycopy(IV, 0, bigIV, 32, IV.length);
//			cipher.init(IV, key);
//			cipherText = cipher.doFinal(plainText);
//			//SHE.doSHE(plainText, key, IV);
//			//cipher.init(IV, key);
//			//decryptedText = cipher.doFinal(cipherText);
//			total+=time.checkNS();
//			
//		}
//		System.out.println(total);
//		average = total/2;
//		System.out.println("Normal: " + average);
//		System.out.println(100/(average/1000000000) + "MB/s");
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
