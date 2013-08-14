/**
 * 
 */
package blackdoor.auth;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import blackdoor.auth.AuthRequest.Operation;

/**
 * @author kAG0
 *
 */
public class AuthServer {
	private int port = 1234;
	private ServerSocket serverSocket;
	private boolean running = true;
	private AuthManager authManager;
	/**
	 * @param args valid args are -port port#, -db userDB-file-location
	 */
	public static void main(String[] args) {
		AuthServer server = new AuthServer();
		
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("-port")){
				server.setPort(Integer.parseInt(args[i++]));
			}
			else if(args[i].equalsIgnoreCase("-db")){
				server.createManager( args[i++]);
			}
			else System.err.println("invalid argument:" + args[i]);
		}
		
		server.start();

	}
	
	AuthServer(){
	}
	
	public void createManager(){
		authManager = new AuthManager();
	}
	public void createManager(String DBFile){
		try {
			authManager = new AuthManager(DBFile);
		} catch (IOException e) {
			System.err.println("specified file is corrupted or not a UserDB");
			e.printStackTrace();
			System.out.println("server will now quit.");
			System.exit(-1);
		}
	}
	
	public void start(){
		if(authManager == null){
			authManager = new AuthManager();
		}
		CLI cli = new CLI(this);
		cli.start();
		listen(port);
		acceptConnections();
	}
	
	public void save(String location){
		try {
			authManager.save(location);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.err.println("save location not valid, database not saved");
		}
	}
	
	private void acceptConnections(){
		ArrayList<AuthConnectionHandler> threads = new ArrayList<AuthConnectionHandler>();
		while(running){
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				System.out.println("connection accepted from " + socket.getRemoteSocketAddress());
			} catch (IOException e) {
				System.err.println("Unable to accept connection on port " + port);
				e.printStackTrace();
			}
			AuthConnectionHandler handler = new AuthConnectionHandler(socket, authManager);
			handler.start();
			threads.add(handler);
		}
	}
	
	private void listen(int port){
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("listenting on port: " + port +".");
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port + ".");
			//e.printStackTrace();
			System.err.println("Server will now quit.");
			System.exit(-1);
		}
	}
	
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	class CLI extends Thread{
		AuthServer server;
		CLI(AuthServer server){
			this.server = server;
		}
		public void run(){
			Scanner keyboard = new Scanner(System.in);
			String input;
			System.out.println("exit to quit, save to save");
			while(true){
				input = keyboard.nextLine();
				if(input.equalsIgnoreCase("exit")){
					server.setRunning(false);
					keyboard.close();
					System.exit(1);
				}
				else if(input.equalsIgnoreCase("save")){
					System.out.println("enter location to save DB");
					String location = keyboard.nextLine();
					System.out.println(location);
					server.save(location);
				}
				else System.out.println("Invalid Command");
			}
		}
	}
	class AuthConnectionHandler extends Thread {
		private Socket socket;
		private AuthManager manager;
		private OutputStream outputBuffer;
		private ObjectOutput outputObject;
		private InputStream inputBuffer;
		private ObjectInput inputObject;
		AuthConnectionHandler(Socket socket, AuthManager manager){
			this.socket = socket;
			this.manager = manager;
		}
		public void run(){
			System.out.println("herp derp, I should reply");
			openSocketInput();
			AuthRequest request = recieveRequest();
			boolean operationCompleted = false;
			switch (request.getOperation()) {
			case ADD:  			operationCompleted=manager.addUser(request.getUserName(), request.getPasswordHash(), request.getRights(), request.getAuthUserName(), request.getAuthPasswordHash());
                     break;
			case CHANGENAME:  	operationCompleted=manager.changeUserName(request.getUserName(), request.getNewUserName(), request.getAuthUserName(), request.getAuthPasswordHash());
                     break;
			case CHANGEPASSWORD:operationCompleted=manager.changePassword(request.getUserName(), request.getPasswordHash(), request.getNewPasswordHash());
                     break;
			case CHECK:  		operationCompleted=manager.checkUser(request.getUserName(), request.getPasswordHash());
                     break;
			case REMOVE:  		operationCompleted=manager.removeUser(request.getUserName(), request.getAuthUserName(), request.getAuthPasswordHash());
                     break;
                    }
			AuthReply reply = new AuthReply(operationCompleted, request.getID(), request.getOperation());
			openSocketOutput();
			sendReply(reply);
			try {
				closeSocket();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void openSocketInput(){
			try {
				inputObject = new ObjectInputStream(socket.getInputStream());
				//inputBuffer = new BufferedInputStream(socket.getInputStream());
				//inputObject = new ObjectInputStream(inputBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private void openSocketOutput(){
			try {
				outputBuffer = new BufferedOutputStream(socket.getOutputStream());
				outputObject = new ObjectOutputStream(outputBuffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private AuthRequest recieveRequest(){
			AuthRequest request = null;
			try {
				request = (AuthRequest) inputObject.readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return request;
		}
		
		private void sendReply(AuthReply reply){
			try {
				outputObject.writeObject(reply);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void closeSocket() throws IOException{
			System.out.println("attempting to close conneciton from " + socket.getRemoteSocketAddress() + " on " + socket.getLocalPort());
			try{
				inputObject.close();
				inputBuffer.close();
				outputObject.close();
				outputBuffer.close();
				socket.close();
			}catch(NullPointerException e){
				System.err.println("Couldn't close connections. Was the connection reset?");
			}
		}
	}
}
