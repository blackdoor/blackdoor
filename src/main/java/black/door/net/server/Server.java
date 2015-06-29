package black.door.net.server;

import black.door.util.DBP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Node server that accepts and handles RPCs.
 * <p>
 * 
 * @author Cj Buresch, Nathan Fischer
 * @version v1.0.0 - June 11, 2015
 */
public class Server implements Runnable {

	public final static int DEFAULT_PORT = 1776;
	/**
	 * Multiplied by the number of cores in the system, this determines how many incoming connections will sit idle
	 * before the server stops accepting connections.
	 */
	public static final double DEFAULT_QUEUE_SIZE_FACTOR = 4;
	/**
	 * Multiplied by the number of cores in the system, this determines the minimum number of threads that will always
	 * be alive, even if there are no connections.
	 */
	public static final double DEFAULT_CORE_THREAD_POOL_SIZE_FACTOR = 1.33333333333333333333333333333333333333333333333;
	/**
	 * Multiplied by the number of cores in the system, this determines how many connections will be handled at once
	 * before new connections begin to be queued.
	 */
	public static final double DEFAULT_MAX_THREAD_POOL_SIZE_FACTOR = 8;
	/**
	 * The ammount of time, in seconds, that threads will continue to sit idle with no connection to handle before
	 * ending.
	 */
	public static final int DEFAULT_TIMEOUT = 30;

	/*
	 * Use For testing.
	 */
	public static void main(String[] args) {
		DBP.VERBOSE = true;
		Server server = new Server(new EchoThread.EchoThreadBuilder());
		server.run();
	}

	private int timeout = DEFAULT_TIMEOUT;
	private double minThreadPoolSizeFactor = DEFAULT_CORE_THREAD_POOL_SIZE_FACTOR;
	private double maxThreadPoolSizeFactor = DEFAULT_MAX_THREAD_POOL_SIZE_FACTOR;
	private double queueSizeFactor = DEFAULT_QUEUE_SIZE_FACTOR;
	private int port;
	private ServerSocket serverSocket;
	private boolean running = false;
	private ThreadPoolExecutor pool;
	private ServerThread.ServerThreadBuilder threadBuilder;
	private BlockingQueue<Runnable> blockingQueue;


	/**
	 *
	 */
	public Server(ServerThread.ServerThreadBuilder builder) {
		this(builder, DEFAULT_PORT);
	}

	/**
	 * Initialize with specific port.
	 * 
	 * @param port
	 */
	public Server(ServerThread.ServerThreadBuilder builder, int port) {
		this.port = port;
		this.threadBuilder = builder;
	}

	private void initialize(){
		int cpus = Runtime.getRuntime().availableProcessors();
		blockingQueue = new LinkedBlockingQueue<>((int)(queueSizeFactor * cpus));
		DBP.printdebugln("Server Detects " + cpus + " cores.");
		int core = (int)(minThreadPoolSizeFactor * cpus);
		int max = (int)(maxThreadPoolSizeFactor * cpus);
		TimeUnit time = TimeUnit.SECONDS;
		pool = new ThreadPoolExecutor(core, max, timeout, time, blockingQueue);
	}


	/**
	 * Starts the server.
	 * <p>
	 * 
	 */
	@Override
	public void run() {
		initialize();
		running = true;
		openServerSocket();
		while (this.isRunning()) {
			try {
				Socket sock = this.serverSocket.accept();
				ServerThread thread = threadBuilder.build(sock);
				if(thread != null)
					pool.execute(thread);
			} catch (IOException e) {
				DBP.printerror("Could not accept socket connection!");
				DBP.printException(e);
			}
		}
	}

	/**
	 * Stops the running server.
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
	 * Returns the boolean status of the server.
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public double getMinThreadPoolSizeFactor() {
		return minThreadPoolSizeFactor;
	}

	public void setMinThreadPoolSizeFactor(double minThreadPoolSizeFactor) {
		this.minThreadPoolSizeFactor = minThreadPoolSizeFactor;
	}

	public double getMaxThreadPoolSizeFactor() {
		return maxThreadPoolSizeFactor;
	}

	public void setMaxThreadPoolSizeFactor(double maxThreadPoolSizeFactor) {
		this.maxThreadPoolSizeFactor = maxThreadPoolSizeFactor;
	}

	public double getQueueSizeFactor() {
		return queueSizeFactor;
	}

	public void setQueueSizeFactor(double queueSizeFactor) {
		this.queueSizeFactor = queueSizeFactor;
	}
}
