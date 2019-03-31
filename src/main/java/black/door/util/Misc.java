package black.door.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class Misc {

	public static final String CLRF = "\r\n";

	public static String arrayToString(Object[] array){
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for(Object o : array){
			sb.append(String.valueOf(o));
			sb.append(", ");
		}
		if(sb.length() > 2)
			sb.setLength(sb.length() -2);
		sb.append(" ]");
		return sb.toString();
	}
	
	public static  String getISO8601ZULUTime(){
		synchronized(ISO8601ZULU){
			return ISO8601ZULU.format(new Date());
		}
	}
	
	public static final DateFormat ISO8601ZULU;
	static{
		ISO8601ZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		ISO8601ZULU.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 *
	 * @param c
	 * @return the zero parameter constructor for c (or null if something weird happens)
	 * @throws SecurityException if the zero param constructor could not be made accessible
	 */
	public static Constructor getZeroParamConstructor(Class c) throws SecurityException, NoSuchMethodException {
		Constructor[] constructors = c.getConstructors();

		for(Constructor con :constructors){
			if(con.getParameterCount() == 0){
				if(! con.isAccessible()){
					con.setAccessible(true);
				}
				return con;
			}
		}

		throw new NoSuchMethodException("Could not find a zero parameter constructor for " + c.getCanonicalName());
	}
	
	public static final char NULL = '\u0000';
	/**
	 * Note, uses Big-endian, so if a is larger than b, then b will have padded 0's on the larger indexed side of the byte array
	 * @param a
	 * @param b
	 * @return The Hamming distance between a and b
	 */
	public static int getHammingDistance(byte[] a, byte[] b){
		int d = 0;
		int i;
		for(i = 0; i < Math.min(a.length, b.length); i++){
			d += bitCount((byte) (a[i]^b[i]));
		}
		for(i = i; i < Math.max(a.length, b.length); i++){
			d += bitCount(
					a.length > b.length
					? a[i]
					: b[i]);
		}
		return d;
	}
	
	/**
	 * Convert the bytes of an IPv4 address to an "IPv4-mapped IPv6 address" according to RFC2373
	 * @param v4 32 bits representing an IPv4 address
	 * @return 16 bytes representing an IPv4-mapped IPv6 address for v4
	 */
	public static byte[] v426(byte[] v4){
		if(v4.length != 4){
			throw new RuntimeException("v4 must be 4 bytes that represent an IPv4 address.");
		}
		byte[] v6 = new byte[16];
		System.arraycopy(v4, 0, v6, 12, 4);
		System.arraycopy(new byte[]{(byte) 0xff, (byte) 0xff}, 0, v6, 10, 2);
		return v6;
	}
	
	/**
	 * Same as getHammingDistance but uses java's BitSet class
	 * Probably not as quick as getHammingDistance
	 * @param a
	 * @param b
	 * @return
	 */
	public static int getCardinalXOR(byte[] a, byte[] b){
		BitSet aSet= BitSet.valueOf(a);
		aSet.xor(BitSet.valueOf(b));
		return aSet.cardinality();
	}
	
	public static int bitCount(byte i) {
		// HD, Figure 5-2
		i = (byte) (i - ((i >>> 1) & 0x55555555));
		i = (byte) ((i & 0x33333333) + ((i >>> 2) & 0x33333333));
		i = (byte) ((i + (i >>> 4)) & 0x0f0f0f0f);
		i = (byte) (i + (i >>> 8));
		i = (byte) (i + (i >>> 16));
		return i & 0x3f;
	}
	
	/**
	 * Get the byte representation of num
	 * @param num
	 * @return
	 */
	public static byte[] getNumberInBytes(Integer num){
		byte[] result = new byte[Integer.SIZE/8];
		int x;
		for(int i = 0; i < result.length; i++){
			x = (Integer.SIZE - (8 + 8*i));
			//System.out.println(x);
			result[i] = (byte) (num.intValue() >> x);
		}
		return result;
	}
	
	/**
	 * Get the byte representation of num
	 * @param num
	 * @return
	 */
	public static byte[] getNumberInBytes(Long num){
		byte[] result = new byte[Long.SIZE/8];
		for(int i = 0; i < result.length; i++){
			result[i] = (byte) (num.longValue() >> (Long.SIZE - (8 + 8*i)));
		}
		return result;
	}
	
	
	/**
	 * Prints JVM memory utilization statistics
	 * author: Viral Patel
	 *
	 * @param runtime
	 */
	public static void PrintMemInfo(Runtime runtime){
		int mb = 1024*1024;
		//Print used memory
	    System.out.println("Used Memory:"
	        + (runtime.totalMemory() - runtime.freeMemory()) / mb);
	    //Print free memory
	    System.out.println("Free Memory:"
	        + runtime.freeMemory() / mb);
	    //Print total available memory
	    System.out.println("Total Memory:" + runtime.totalMemory() / mb);
	}
	
	public static String getHexBytes(byte[] in, String space){
		String hexChars = "";
		int v;
		for ( int j = 0; j < in.length; j++ ) {
			v = in[j] & 0xFF;
			hexChars += hexArray[v >>> 4];
			hexChars += hexArray[v & 0x0F];
			hexChars += space;
		}
		return hexChars.substring(0, hexChars.length() - space.length());
	}
	
	/**
	 * Very fast method to get hex string from byte array.
	 * credit to maybeWeCouldStealAVan and others on stackoverflow 
	 */
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**
	 * Serialize an object into a byte array
	 * @param s a Serializable object
	 * @return s serialized into a byte array
	 * @throws IOException
	 */
	public static byte[] serialize(Serializable s) throws IOException{
		byte[] out;
		ByteArrayOutputStream byteOutS = new ByteArrayOutputStream();
		ObjectOutputStream objOutS = new ObjectOutputStream(byteOutS);
		objOutS.writeObject(s);
		out = byteOutS.toByteArray();
		objOutS.close();
		return out;
	}
	
	/**
	 * Performs an XOR operation on two arrays of bytes, byte by byte.
	 * The returned array will be the same length as the longest parameter array,
	 * the shorter array will be padded with 0's.
	 * Has the same memory concerns as cleanXOR
	 * @param array1
	 * @param array2
	 * @return array1 XOR array2.
	 */
	public static byte[] XOR(byte[] array1, byte[] array2){
		int length;
		boolean trueIfOne;
		if(array1.length >= array2.length){
			length = array1.length;
			trueIfOne = true;
		}
		else {
			length = array2.length;
			trueIfOne = false;
		}
		byte[] array3 = new byte[length];
		int i = 0;
		if(trueIfOne){
			for(byte a:array2){
				array3[i] = (byte) (a ^ array1[i++]);
			}
			//return array1;
			System.arraycopy(array1, i, array3, i, length-i);
		}
		else{
			for(byte a:array1){
				array3[i] = (byte) (a ^ array2[i++]);
			}
			//return array2;
			System.arraycopy(array2, i, array3, i, length-i);
		}
		return array3;
	}
	
	/** 
	 * Same as XOR, but uses java's BitSet.
	 * performance comparison not yet tested.
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static byte[] XOR2(byte[] array1, byte[] array2){
		BitSet aSet= BitSet.valueOf(array1);
		aSet.xor(BitSet.valueOf(array2));
		return aSet.toByteArray();
	}
	
	/*
	 * this xor's the parameters, which must be the same length. 
	 * after calling both a and b will be changed to the same xor
	 */
	public static void XORValues(byte[] a, byte[] b){
		if(a.length != b.length)
			throw new RuntimeException("parameters are not same length");
		for(int i = 0; i < a.length; i++){
			a[i] = b[i] = (byte) (a[i]^b[i]);
		}
	}
	
	/*
	 * this xor's the parameters, which must be the same length. 
	 * after calling only a will be changed to a xor b
	 * @return a
	 */
	public static byte[] XORintoA(byte[] a, byte[] b){
		if(a.length != b.length)
			throw new RuntimeException("Parameters are not same length for xor.");
		for(int i = 0; i < a.length; i++){
			a[i] = (byte) (a[i]^b[i]);
		}
		return a;
	}
	/**
	 * returns a XOR b, leaves a and b unchanged
	 * uses 3n memory (or more depending on array size and JVM settings) where n is the length of a &amp; b.
	 * does no error checking to make sure a &amp; b are same length
	 * @param a
	 * @param b
	 * @return a XOR b
	 */
	public static byte[] cleanXOR(byte[] a, byte[] b){
		byte[] c = new byte[a.length];
		int i=0;
		for (byte d : b)
		    c[i] = (byte) (d ^ a[i++]);
		return c;
	}
	
	public static void arraycopy(byte[] source, int srcPos, byte[] dest, int destPos, int length){
		for(int i = 0; i < length; i++){
			dest[i + destPos] = source[i + srcPos];
		}
	}
	
	/**
	 * clear the terminal screen by overwriting it with 100 blank lines.
	 */
	public static void cls(){
		for(int i = 0; i < 100; i++){
			System.out.println();
		}
	}
	
	/**
	 * @param a
	 * @param b
	 * @param c
	 * @return (a^b)%c
	 */
	public static long longBMP(long a, long b, long c){
		return BigInteger.valueOf(a).modPow(BigInteger.valueOf(b), BigInteger.valueOf(c)).longValue();
	}

	@SafeVarargs
	public static <T> T[] array(T... array){
		return array;
	}

	@SafeVarargs
	public static <T> List<T> list(T... list){
		return Arrays.asList(list);
	}

	@SafeVarargs
	public static <T> Set<T> set(T... set){
		return new HashSet<>(list(set));
	}

	public static void print(Object o){
		System.out.print(o);
	}

	public static void println(Object o){
		System.out.println(o);
	}

	public static void require(boolean r){
		if(!r)
			throw new IllegalArgumentException();
	}

	public static void require(boolean r, CharSequence message){
		if(!r)
			throw new IllegalArgumentException(message.toString());
	}

}
