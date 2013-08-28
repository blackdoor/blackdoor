package blackdoor.util;

import blackdoor.util.Crypto.EncryptionResult;
import blackdoor.util.Crypto.InvalidKeyLengthException;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Crypto hasher = new Crypto();

		EncryptionResult cypher;
		try {
			cypher = Crypto.getAESEncryption("Hello world".getBytes(), "password".getBytes(), 128);
			System.out.println(new String(Crypto.getAESDecryption(cypher.getCipherText(), "password".getBytes(), cypher.getSalt(), cypher.getIv(), 256)));
		} catch (InvalidKeyLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(new String(Crypto.getAESDecryption(cypher.getCipherText(), "password".getBytes(), cypher.getSalt(), cypher.getIv(), 256)));
		
	}

}
