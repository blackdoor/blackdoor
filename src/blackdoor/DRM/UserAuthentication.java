/**
 * 
 */
package blackdoor.DRM;

/**
 * @author kAG0
 *
 */
public class UserAuthentication {
	private byte[] passwordHash;
	private byte[] authenticityHash;
	
	public void generateAuthHash(byte[] PSK){
		authenticityHash = new byte[passwordHash.length];
		for(int i = 0; i < passwordHash.length; i++){
			authenticityHash[i] = (byte) (passwordHash[i] ^ PSK[i]);
		}
	}
	
}
