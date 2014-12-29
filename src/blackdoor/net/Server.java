package blackdoor.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import blackdoor.net.ServerThread.ServerThreadBuilder;
import blackdoor.util.DBP;

/**
 * Node server that accepts and handles RPCs.
 * <p>
 * 
 * @author Cj Buresch
 * @version v0.1.0 - Dec. 12, 2014
 */
public class Server implements Runnable {

	private final static int DEFAULT_PORT = 1776;
	private int port;
	private ServerSocket serverSocket;
	private boolean running = false;
	private ThreadPoolExecutor pool;
	private ServerThreadBuilder threadBuilder;
	private BlockingQueue<Runnable> blockingQueue;
	private Thread runningThread = null;
	private final int QUEUE_SIZE = 256;// ?

	/**
	 * Initialize from configuration file settings.
	 * <p>
	 */
	public Server(ServerThreadBuilder builder) {
		this(builder, DEFAULT_PORT);
	}

	/**
	 * Initialize with specific port.
	 * 
	 * @param port
	 */
	public Server(ServerThreadBuilder builder, int port) {
		this.port = port;
		blockingQueue = new ArrayBlockingQueue<Runnable>(QUEUE_SIZE);
		pool = getPool();
		this.threadBuilder = builder;
	}

	/*
	 * Use For testing.
	 */
	public static void main(String[] args) {
		DBP.VERBOSE = true;
		Server server = new Server(new EchoThread.EchoThreadBuilder());
		server.run();
	}

	/**
	 * Starts the node server.
	 * <p>
	 * 
	 */
	@Override
	public void run() {
		running = true;
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while (this.isRunning()) {
			try {
				Socket sock = this.serverSocket.accept();
				ServerThread thread = threadBuilder.build(sock);
				pool.execute(thread);
			} catch (IOException e) {
				DBP.printerror("Could not accept socket connection!");
				DBP.printException(e);
			}
		}
	}

	/**
	 * Stops the running node server.
	 * <p>
	 * 
	 */
	public synchronized void stop() {
		running = false;
		pool.shutdown();
		try {
			if (!pool.awaitTermination(60, TimeUnit.SECONDS))
				pool.shutdownNow();
			if (!serverSocket.isClosed())
				this.serverSocket.close();
		} catch (IOException e) {
			DBP.printerror("Error closing Socket.");
			DBP.printException(e);
		} catch (InterruptedException ie) {
			DBP.printerror("Error shutting down threadpool.");
			DBP.printException(ie);
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Returns the boolean status of the node server.
	 * <p>
	 * 
	 * @return
	 */
	public synchronized boolean isRunning() {
		return this.running;
	}

	/**
 * 
 */
	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			running = false;
			DBP.printerror("COULD NOT OPEN SERVERSOCKET on port: " + port);
			DBP.printException(e);
		}
	}

	/**
	 * 
	 * @return
	 */
	private ThreadPoolExecutor getPool() {
		int cpus = Runtime.getRuntime().availableProcessors();
		DBP.printdevln("Server Detects " + cpus + " cores.");
		int core = 5 * cpus;
		int max = 15 * cpus;
		int timeout = 60;
		TimeUnit time = TimeUnit.SECONDS;
		ThreadPoolExecutor tmp = new ThreadPoolExecutor(core, max, timeout,
				time, blockingQueue);
		return tmp;
	}

}
