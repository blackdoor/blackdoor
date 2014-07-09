package blackdoor.util;

import java.util.ArrayList;
import java.util.List;

public class CommandLineParser {
	
	//private String [] args;
	private List<List<String>> sortedArgs = null;
	private List<Option> options = null;
	private List<Option> nonOptions;
	private String usageHint = "";
	private String executableName = "";
	
	public CommandLineParser(){
		sortedArgs = new ArrayList<List<String>>();
		options = new ArrayList<Option>();
		nonOptions = new ArrayList<Option>();
	}
	
//	/**
//	 * Get the parsed and checked command line arguments for this parser
//	 * non-option parameters are assigned the -- name from the non-option parameters FIFO based on when the non option parameter was added to the parser
//	 * @return A list of strings, the first([0]) element in each list
//	 * 		is the command line option, if the second([1]) element exists it is the 
//	 * 		parameter for that option.
//	 * 		Returns null if parseArgs(String[]) has not been called.
//	 */
//	public List<List<String>> getParsedArgs(){
//		return sortedArgs;
//	}
	
	/**
	 * Get the parsed and checked command line arguments for this parser
	 * @param args - The command line arguments to add. These can be passed straight from the parameter of main(String[])
	 * @return A list of strings, the first([0]) element in each list
	 * 		is the command line option, if the second([1]) element exists it is the 
	 * 		parameter for that option.
	 * 		Returns null if parseArgs(String[]) has not been called.
	 */
	public List<List<String>> getParsedArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if(!args[i].startsWith("-")){
				if(nonOptions.size() > 0){
					List<String> option = new ArrayList<String>();
					option.add(nonOptions.get(0).longOption);
					nonOptions.remove(0);
					option.add(args[i]);
					sortedArgs.add(option);
				}else{
					throw new InvalidFormatException("Expected command line option, found " +
							args[i] + " instead.");
				}	

			}else{

				for (Option option : options) {
					if (option.matchesFlag(args[i])) {
						List<String> command = new ArrayList<String>();
						command.add(noDashes(args[i]));
						if (option.hasParam) {
							try {
								if (args[i + 1].startsWith("-")) {
									if (option.paramRequired)
										throw new InvalidFormatException(
												"Invalid command line format: -"
														+ option.option
														+ " or --"
														+ option.longOption
														+ " requires a parameter, found "
														+ args[i + 1] + " instead.");
								} else {
									command.add(args[++i]);
								}
							} catch (ArrayIndexOutOfBoundsException e) {
							}
						}
						sortedArgs.add(command);
						break;
					}
				}}
		}
		return sortedArgs;
	}

	public String getHelpText(){
		String ret = "Usage: " + executableName + " [OPTION]... ";
		for(Option opt : options){
			if(opt.nonOptionParam)
				ret += opt.longOption + " ";
		}
		ret += "\n" + usageHint + "\n";
		for(Option option : options){
			if(!option.nonOptionParam){
				ret += "\t-" + option.option + "\t--" + option.longOption;
				ret += "\n\t\t\t" + option.helpText + "\n";
			}
		}
		return ret;
	}
	
	/**
	 * @param usageHint the usageHint to set for the program who's command line arguments are being parsed
	 */
	public void setUsageHint(String usageHint) {
		this.usageHint = usageHint;
	}

	/**
	 * @return the executableName
	 */
	public String getExecutableName() {
		return executableName;
	}

	/**
	 * @param executableName the executableName to set
	 */
	public void setExecutableName(String executableName) {
		this.executableName = executableName;
	}

	/**
	 * adds options for this command line parser
	 * @param optionList a list of Strings of options in a comma separated format
	 * 		single char options should be prepended with a single "-"
	 * 		string options should be prepended with "--"
	 * 		non-option parameters should add a "?" to the string. non-option parameters should define the string (long/--) form option.
	 * 			ie. parameters that would not have an explicit option before them
	 * 			eg. cp source.txt dest.txt
	 * 		to add helptext for this option, add -h followed by the help text
	 * 		if there MUST be a parameter after this command line option then add a "+" to the string
	 * 		alternatively if there MAY be a parameter after this command line option then add a "*" to the string
	 * 		eg. "-r.--readonly" or "--file,-f,+" or "*, -f, --flag"
	 */
	public void addOptions(String [] optionList){
		for(String option : optionList){
			Option f = new Option();
			String[] breakdown = option//.replaceAll("\\s", "")
					.split(",");
			for(String s : breakdown){
				s = s.trim();
				if(s.startsWith("--")){
					f.longOption = noDashes(s);
				}else if(s.startsWith("-h")){
					f.helpText = s.substring(2);
				}else if(s.startsWith("-")){
					f.option = noDashes(s);
				}else if(s.equals("+")){
					f.hasParam = true;
					f.paramRequired = true;
				}else if(s.equals("?")){
					f.nonOptionParam = true;
					nonOptions.add(f);
				}else if(s.equals("*")){
					f.hasParam = true;
				}else{
					throw new InvalidFormatException(s + " in " + option + " is not formatted correctly.");
				}
			}
			options.add(f);
		}
		System.out.println(options);
	}
	
	/**
	 * Add a single option to this command line parser.
	 * Convenience method for calling addFlags with only one option
	 * @param optionString - formatted string for this command line option, should be in same format as strings in addOptions(String[])
	 */
	public void addOption(String optionString){
		addOptions(new String[]{optionString});
	}
	
	/**
	 * Add a option for this command line parser. Parameters should not be prepended by dashes.
	 * @param shortForm - Single character command line option
	 * @param longForm	- String command line option
	 * @param hasParameter - Should be true if this option will be followed by a parameter when called from the command line.
	 */
	public void addOption(char shortForm, String longForm, String helpText, boolean nonOptionParam, boolean hasParameter, boolean parameterRequired){
		Option f = new Option();
		f.option = ""+shortForm;
		f.longOption = longForm;
		f.hasParam = hasParameter;
		f.paramRequired = parameterRequired;
		f.helpText = helpText;
		f.nonOptionParam = nonOptionParam;
		if(nonOptionParam)
			nonOptions.add(f);
		options.add(f);
	}
	
	private String noDashes(String s){
		return s.replaceFirst("(--|-)", "");
	}
	
	
	private class Option{
		
		String option = "";
		String longOption = "";
		String param = null;
		String helpText = "";
		boolean hasParam = false;
		boolean paramRequired = false;
		boolean nonOptionParam = false;
		
		public boolean matchesFlag(String option){
			return !nonOptionParam && (option.equalsIgnoreCase("--"+longOption) || option.equals("-"+this.option));
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Option [option=" + option + ", longOption=" + longOption
					+ ", param=" + param + ", hasParam=" + hasParam
					+ ", paramRequired=" + paramRequired + " helpText=" + helpText +"]";
		}

		
		
	}
	
	public class InvalidFormatException extends RuntimeException{ 
		InvalidFormatException(String s){
			super(s);
		}
	}

}
