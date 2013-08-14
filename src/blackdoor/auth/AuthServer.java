/**
 * 
 */
package blackdoor.auth;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

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
		Socket socket;
		AuthManager manager;
		AuthConnectionHandler(Socket socket, AuthManager manager){
			this.socket = socket;
			this.manager = manager;
		}
		public void run(){
			System.out.println("herp derp, I should reply");
		}
	}
}
