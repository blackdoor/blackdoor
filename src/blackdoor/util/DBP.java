/**
 * 
 */
package blackdoor.util;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


/**
 * DeBugPrint. A class that allows output to be categorized before being
 * printed. Different categories of output can then be enabled, disabled or
 * redirected at runtime. The default output for most categories is System.out
 * 
 * @author nfischer3
 * 
 */
public enum DBP {
	INSTANCE;
	
	public static enum DefaultChannelNames{
		DEBUG, ERROR, DEV, DEMO, WARNING, LOG;
	}
	
	public static class Channel{
		
		private String name;
		private boolean enabled;
		private boolean printAsJson;
		private boolean log;
		private boolean neverLog;
		private boolean printStack;
		private boolean printLine;
		
		private PrintStream pStream;
		
		public Channel(String name, OutputStream stream){
			this.name = name.toUpperCase();
			setOutputStream(stream);
			enabled = true;
			printAsJson = false;
			log = false;
			printStack = false;
			printLine = false;
			neverLog = false;
		}
		
		private void print(Object... o){
			if(enabled || VERBOSE){
				String formatted = getFormatted(o);
				pStream.print(formatted);
				if((log || LOG_ALL) && !neverLog)
					getChannels().get("LOG").pStream.print(formatted);			
			}
		}
		
		/**
		 * For a number of objects, returns the String which would be printed if
		 * this Channel's print(Object..) method were called.
		 * 
		 * @param o
		 *            between one and many objects. If JSON printing is enabled
		 *            for this Channel, Map and List objects will be added as
		 *            JSON objects and JSON arrays, Numbers and Booleans will
		 *            also be converted to their respective JSON types. Objects
		 *            which cannot be converted to a JSON type will become
		 *            strings by the definition of String.valueOf(Object).
		 * @return
		 */
		private String getFormatted(Object... o){
			StringBuilder sb = new StringBuilder();			
			String time = Misc.getISO8601ZULUTime();
			StackTraceElement line = null;
			if(printLine)
				line = Thread.currentThread().getStackTrace()[5];
			
			if(printAsJson){
				org.json.JSONObject js = new org.json.JSONObject();
				js.put("timestamp", time);
				js.put("channel", name);
				if(printLine)
					js.put("line", line);
				org.json.JSONArray arr = new org.json.JSONArray();
				for(Object obj : o){
					
					arr.put(obj);
				}
				js.put("message", arr);
				sb.append(js);
				sb.append('\n');
			}else{
				String header = "[" + time + "]" + String.format("[%-7s] ", name);
				StringBuilder sb2 = new StringBuilder();
				for(Object obj : o){
					sb2.append(String.valueOf(obj));
				}
				if(printStack){
					StackTraceElement[] stack = Thread.currentThread().getStackTrace();
					sb2.append('\n');
					for(int i = 5; i < stack.length; i++){
						sb2.append("  at " + stack[i] + '\n');
					}
				}
				Scanner scan = new Scanner(sb2.toString());
				while(scan.hasNextLine()){
					sb.append(header + scan.nextLine());
					if(printLine && !printStack)
						sb.append(" at " + line);
					sb.append('\n');
				}
				scan.close();
			}
			
			return sb.toString();
		}

		public String getName() {
			return name;
		}
		
		

		/**
		 * @return the printLine
		 */
		public boolean isPrintingLine() {
			return printLine;
		}

		/**
		 * @param printLine the printLine to set
		 */
		public void setPrintLine(boolean printLine) {
			this.printLine = printLine;
		}

		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * @return the printStack
		 */
		public boolean isPrintingStack() {
			return printStack;
		}

		/**
		 * @param printStack the printStack to set
		 */
		public void setPrintStack(boolean printStack) {
			this.printStack = printStack;
		}

		public boolean enable(){
			boolean temp = enabled;
			enabled = true;
			return temp;
		}
		
		public boolean disable(){
			boolean temp = enabled;
			enabled = false;
			return temp;
		}
		
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}


		public boolean printsAsJson() {
			return printAsJson;
		}


		public void printAsJson(boolean printAsJson) {
			this.printAsJson = printAsJson;
		}


		public boolean isLogging() {
			return log;
		}
		
		public void setNeverLog(boolean b){
			this.neverLog = b;
		}
		
		public boolean getNeverLog(){
			return neverLog;
		}


		public void setLogging(boolean log) {
			this.log = log;
		}

		public OutputStream getOutputStream() {
			return pStream;
		}

		public void setOutputStream(OutputStream stream) {
			if(stream instanceof PrintStream)
				pStream = (PrintStream) stream;
			else
				pStream = new PrintStream(stream);
		}
		
		public void setOutputStream(OutputStream stream, boolean close) {
			if(pStream != null && close)
				pStream.close();
			setOutputStream(stream);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Channel [name=" + name + ", enabled=" + enabled
					+ ", printingAsJson=" + printAsJson + ", logging=" + log
					+ ", printStream=" + pStream + "]";
		}
		
	}
	
	private Map<String, Channel> channels;
	
	/*
	public static boolean DEBUG = false;
	public static boolean ERROR = false;
	public static boolean DEV = false;
	public static boolean DEMO = false;
	public static boolean WARNING = false;
	public static boolean LOG = true;
	*/
	
	private static Map<String, Channel> getChannels(){
		return INSTANCE.channels;
	}
	
	/**
	 * The location of the file where calls to printlog and printlogln are
	 * written.
	 */
	private static final String DEFAULT_LOG_FILE = "log.log";
	/**
	 * When true, all calls to printerror and printerrorln are written to
	 * System.err rather than the default output.
	 */
	//public static boolean ERROR_AS_SYSTEM_ERROR = true;
	
	/**
	 * When true, all output is written to the log file in addition to it's
	 * usual output.
	 */
	public static boolean LOG_ALL = false;
	/**
	 * When true, calls to any print* or print*ln method act as if their
	 * corresponding flag is true. In other words all calls will result data
	 * being written to output. The exception to this is log, which is
	 * unaffected.
	 */
	public static boolean VERBOSE = false;

	private PrintStream out = System.out;
	private PrintStream log;

	private DBP() {
		channels = new ConcurrentHashMap<String, Channel>();
		
		Channel debug = new Channel("DEBUG", System.out);
		debug.setEnabled(false);
		Channel error = new Channel("ERROR", System.err);
		Channel dev = new Channel("DEV", System.out);
		dev.disable();
		Channel demo = new Channel("DEMO", System.out);
		demo.disable();
		Channel warning = new Channel("WARNING", System.out);
		warning.disable();
		Channel log = null;
		try {
			log = new Channel("LOG", new PrintStream(DEFAULT_LOG_FILE));
			log.disable();
			channels.put(log.getName(), log);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(DEFAULT_LOG_FILE + " could not be created.");
		}
		
		channels.put(debug.getName(), debug);
		channels.put(error.getName(), error);
		channels.put(dev.getName(), dev);
		channels.put(demo.getName(), demo);
		channels.put(warning.getName(), warning);
		
	}
	
	public static void addChannel(String name, OutputStream stream){
		addChannel(name, stream, true);
	}
	
	public static void addChannel(String name, OutputStream stream, boolean enabled){
		Channel c = new Channel(name, stream);
		c.enabled = enabled;
		addChannel(c);
	}
	
	public static void addChannel(Channel channel){
		if(INSTANCE.channels.containsKey(channel.getName()))
			throw new ChannelAlreadyExistsException();
		INSTANCE.channels.put(channel.getName(), channel);
	}
	
	public static Channel removeChannel(String channel){
		return INSTANCE.channels.remove(channel);
	}
	
	public static Channel getChannel(String channel){
		if(!INSTANCE.channels.containsKey(channel.toUpperCase()))
			throw new ChannelNotFoundException();
		return INSTANCE.channels.get(channel.toUpperCase());
	}
	
	public static Channel getChannel(DefaultChannelNames channel){
		return getChannel(channel.name());
	}
	
	public static OutputStream getChannelStream(String channel){
		Channel c = getChannels().get(channel.toUpperCase());
		if(c == null){
			throw new ChannelNotFoundException();
		}
		return c.pStream;
	}
	
	public static boolean enableChannel(String channel){
		Channel c = getChannel(channel);
		boolean old = c.isEnabled();
		c.setEnabled(true);
		return old;
	}
	
	public static boolean disableChannel(String channel){
		Channel c = getChannel(channel);
		boolean old = c.isEnabled();
		c.setEnabled(false);
		return old;
	}
	
	public static void println(String channel, Object... e){
		if(INSTANCE.channels.containsKey(channel.toUpperCase()))
			getChannel(channel).print(e);
		else
			println(DefaultChannelNames.ERROR.toString(), "unknown DBP channel: " + channel + " at " + Thread.currentThread().getStackTrace()[2]);
	}
	
	/**
	 * Prints e to a log file. Use this method for any output to be saved to the
	 * log file (obviously).
	 * 
	 * @param e
	 */
	public static void printlog(Object e) {
		println("LOG", e);//print(e, "LOG", LOG, singleton.log);
	}

	public static void printlogln(Object e) {
		printlog(e.toString() + '\n');
	}

	/**
	 * Use this method for any text that might be useful during debugging; such
	 * as values of variables or states of a FSM.
	 * 
	 * @param e
	 */
	public static void printdebug(Object e) {
		println("DEBUG", e);
	}

	public static void printdebugln(Object e) {
		printdebug(e.toString() + '\n');
	}
	
	/**
	 * Identical to calling Exception.printStackTrace() except with the DBP timestamp and [ERROR] prefix. 
	 * Printed to default output or stderror or not at all according to settings for DBP ERROR
	 * @param e the Exception object to print
	 */
	public static void printException(Exception e){
        printerrorln(e);     
        for(StackTraceElement elem : e.getStackTrace()){
			printerrorln("  at " + elem);
		}
	}

	/**
	 * Use this method to print any errors. Try to make error output easily
	 * readable such that printing errors could be enabled in production code.
	 * 
	 * @param e
	 */
	public static void printerror(Object e) {
		println("ERROR", e);
	}

	public static void printerrorln(Object e) {
		printerror(e.toString());
	}
	
	/**
	 * Use this method for output that should NEVER be seen outside of
	 * development; such as printing the output of a regex while tweaking it or
	 * a line to let you know a method call has been made.
	 * 
	 * @param e
	 */
	public static void printdev(Object e) {
		println("DEV", e);
	}

	public static void printdevln(Object e) {
		printdev(e.toString() + '\n');
	}

	/**
	 * Use this method for output that would be used in a live demo; for example
	 * to show that some variable has changed or that some action has happened.
	 * 
	 * @param e
	 */
	public static void printdemo(Object e) {
		println("DEMO", e);
	}

	public static void printdemoln(Object e) {
		printdemo(e.toString() + '\n');
	}

	/**
	 * Use this to print warnings. Same rules as printerror(); warning output
	 * should be ok to enable in production.
	 * 
	 * @param e
	 */
	public static void printwarning(Object e) {
		println("WARNING", e);
	}

	public static void printwarningln(Object e) {
		printwarning(e.toString() + '\n');
	}

	public static boolean toggleDebug() {
		Channel debug = getChannel("DEBUG");
		debug.setEnabled(!debug.isEnabled());
		return debug.isEnabled();
	}

	public static boolean toggleDemo() {
		Channel c = getChannel(DefaultChannelNames.DEMO.name());
		c.setEnabled(!c.isEnabled());
		return c.isEnabled();
	}

	public static boolean toggleDev() {
		Channel c = getChannel(DefaultChannelNames.DEV.name());
		c.setEnabled(!c.isEnabled());
		return c.isEnabled();
	}

	public static boolean toggleError() {
		Channel c = getChannel(DefaultChannelNames.ERROR.name());
		c.setEnabled(!c.isEnabled());
		return c.isEnabled();
	}

	public static boolean toggleLog() {
		Channel c = getChannel(DefaultChannelNames.LOG.name());
		c.setEnabled(!c.isEnabled());
		return c.isEnabled();
	}

	public static boolean toggleWarning() {
		Channel c = getChannel(DefaultChannelNames.WARNING.name());
		c.setEnabled(!c.isEnabled());
		return c.isEnabled();
	}

	public static boolean toggleVerbose() {
		VERBOSE = !VERBOSE;
		return VERBOSE;
	}

	/**
	 * Sets the location of the log file. Also closes the previous stream for LOG
	 * 
	 * @param logFile
	 *             The location of the file where calls to printlog and
	 *            printlogln are written.
	 * @throws FileNotFoundException 
	 */
	public static void setLogFileLocation(String logFile) throws FileNotFoundException{
		getChannel(DefaultChannelNames.LOG).setOutputStream(new PrintStream(logFile), true);
	}


	private static class ChannelNotFoundException extends RuntimeException {
		public ChannelNotFoundException() {
			super("The channel you specified is not recognized. Check spelling or create it.");
		}
	}
	
	private static class ChannelAlreadyExistsException extends RuntimeException {
		public ChannelAlreadyExistsException() {
			super("The channel you specified already exists. Remove it or modify its settings.");
		}
	}
}
