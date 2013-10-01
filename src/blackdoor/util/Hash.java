/**
*	the cleanness of my commit history was sacrificed in a great battle 
*	of merge conflicts so that this class could live
*/
package blackdoor.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

/**
* @author kAG0
*
*/
public class Hash {
private byte[] input = null;

public Hash(){
}

public Hash(byte[] input){
this.input = input;
}

public void update(byte[] input){
this.input = input;
}

private static byte[] getHash(String algorithm, byte[] input){
if(input == null)
throw new RuntimeException("input not defined");
//byte[] output = null;
MessageDigest mD = null;
try {
mD = MessageDigest.getInstance(algorithm);
} catch (NoSuchAlgorithmException e) {
e.printStackTrace();
}
mD.update(input);
return mD.digest();
//return output;
}

public byte[] getSHA1(){
return getSHA1(input);
}

public static byte[] getSHA1(byte[] input){
return getHash("SHA-1", input);
}

public String getSHA1String(){
return DatatypeConverter.printHexBinary(getSHA1(input));
}
public static String getSHA1String(byte[] input){
return DatatypeConverter.printHexBinary(getSHA1(input));
}

public byte[] getSHA256(){
return getSHA256(input);
}

public static byte[] getSHA256(byte[] input){
return getHash("SHA-256", input);
}

public String getSHA256String(){
return DatatypeConverter.printHexBinary(getSHA256(input));
}
public static String getSHA256String(byte[] input){
return DatatypeConverter.printHexBinary(getSHA256(input));
}

}