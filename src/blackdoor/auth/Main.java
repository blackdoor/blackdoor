package blackdoor.auth;

import java.io.IOException;

import org.apache.commons.codec.binary.Hex;

import blackdoor.auth.User.UserRight;
import blackdoor.util.Hash;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AuthClient client = new AuthClient("localhost", 1234);
		System.out.println(client.checkUser("origin", "pass"));
		UserRight[] rights = {};
		System.out.println(client.removeUser("jim", "origin", "pass"));
		System.out.println(client.addUser("jim", "password", rights, "origin", "pass"));
		System.out.println(client.removeUser("jim", "jim", "password"));
		System.out.println(client.checkUser("jim", "password"));
		
	}

}
