package blackdoor.util;

import blackdoor.util.Crypto.EncryptionResult;
import blackdoor.util.Crypto.InvalidKeyLengthException;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] b = new byte[]{23, 57, 23, 0};
		byte[] a = new byte[]{(byte) 0x1111};
		System.out.println(a);
		System.out.println(b);
		byte[] c = Misc.XOR(a, b);
		System.out.println(c);
	}

}
