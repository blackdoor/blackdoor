package blackdoor.util;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Hash hasher = new Hash();

		String out = hasher.getSHA256().toString();
		System.out.println(out);
		
	}

}
