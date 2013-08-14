/**
 * 
 */
package blackdoor.auth;

import java.io.IOException;

import blackdoor.auth.User.UserRight;

/**
 * @author kAG0
 *
 */
public class AuthManager {
	private UserDB users;
	/**
	 * create a new AuthManager with the default UserDB
	 */
	AuthManager(){
		users = new UserDB();
	}
	/**
	 * create a new AuthManager with the given UserDB file
	 * @param userDBFile
	 * @throws IOException
	 */
	AuthManager(String userDBFile) throws IOException{
		try {
			users = new UserDB(userDBFile);
		} catch (ClassNotFoundException e) {
			System.err.println("Specified file is corrupted or not a UserDB.");
			e.printStackTrace();
		}
	}
	
	public void save(String file) throws IOException{
		users.saveDB(file);
	}
	
	/**
	 * check if a user exists with the given username and password
	 * @param userName
	 * @param password
	 * @return true if a user exists with given username and password, else false
	 */
	public boolean checkUser(String userName, byte[] password) {
		if (users.containsKey(userName)) {
			User userFromDB = users.get(userName);
			if (userFromDB.checkPassword(password)
					&& userFromDB.getUserName().equalsIgnoreCase(userName)) {
				return true;
			} else
				return false;
		}
		return false;
	}
	
	public boolean changePassword(String userName, byte[] oldPasswordHash, byte[] newPasswordHash){
		if(checkUser(userName, oldPasswordHash)){
			User userFromDB = users.get(userName);
			userFromDB.setPassword(oldPasswordHash, newPasswordHash);
			return true;
		}
		System.err.println("User could not be authenticated or user does not exist");
		return false;
	}
	
	
	/**
	 * add a user with given attributes under the authority of authUser
	 * 
	 * @param userName
	 * @param passwordHash
	 * @param rights
	 * @param authUserName
	 * @param authPasswordHash
	 * @return true if user has been added, else false
	 */
	public boolean addUser(String userName, byte[] passwordHash,UserRight[] rights, String authUserName, byte[] authPasswordHash) {
		if (checkUser(authUserName, authPasswordHash)) {
			if (users.get(authUserName).hasRight(UserRight.ADD)) {
				if (!users.containsKey(userName)) {
					User newUser = new User(userName, passwordHash);
					int i = 0;
					while (i < rights.length) {
						newUser.addUserRight(rights[i++]);
					}
					users.put(newUser);
					return true;
				}
				else System.err.println("User already exists.");
			}
			else System.err.println("Authenticating user does not have proper permissions.");
		}
		else System.err.println("Authenticating user does not exist or has improper validation.");
		return false;
	}
	/**
	 * Remove user with userName. only users with REMOVE rights may remove users, however users may remove themselves without remove rights.
	 * @param userName the user to remove
	 * @param authUserName
	 * @param authPasswordHash
	 * @return true if user has been removed, else false
	 */
	public boolean removeUser(String userName, String authUserName,byte[] authPasswordHash) {
		if (checkUser(authUserName, authPasswordHash)) {
			if (users.get(authUserName).hasRight(UserRight.REMOVE)
					|| authUserName.equalsIgnoreCase(userName)) {
				users.remove(userName);
				return true;
			} else
				System.err
						.println("Authenticating user does not have proper permissions.");
		} else
			System.err
					.println("Authenticating user does not exist or has improper validation.");
		return false;
	}
	/**
	 * change the given user's username 
	 * @param oldUserName
	 * @param newUserName
	 * @param authUserName
	 * @param authPasswordHash
	 * @return true if the operation has completed successfully, else false
	 */
	public boolean changeUserName(String oldUserName, String newUserName, String authUserName, byte[] authPasswordHash){
		if (checkUser(authUserName, authPasswordHash)) {
			if (users.get(authUserName).hasRight(UserRight.CHANGE)) {
				if (users.containsKey(oldUserName)) {
					User user = users.get(oldUserName);
					users.remove(oldUserName);
					user.setUserName(newUserName);
					users.put(user);
					return true;
				}
				else System.err.println("User does not exist.");
			}
			else System.err.println("Authenticating user does not have proper permissions.");
		}
		else System.err.println("Authenticating user does not exist or has improper validation.");
		return false;
	}
}
