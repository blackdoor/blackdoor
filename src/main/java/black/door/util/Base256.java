package black.door.util;

public class Base256 {
	
	private static final String ORACLE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ+-/*=abcdefghijklmnopqrstuvwxyzÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡ΢ΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρςστυφχψωЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъы";
	
	public static String encode(byte[] bytes){
		String out = "";
		for(byte b : bytes){
			out += ORACLE.charAt(0xff & (int)b);
		}
		return out;
	}
	
	public static byte[] decode(String string){
		byte[] bytes = new byte[string.length()];
		for(int i = 0; i < string.length(); i++){
			bytes[i] = (byte) ORACLE.indexOf(string.charAt(i));
		}
		return bytes;
	}
}
