package blackdoor.util;

import java.util.ArrayList;
import java.util.List;

public class CommandLineParser {
	
	//private String [] args;
	public List<List<String>> sortedArgs = null;
	public ArrayList<Parameter> options = null;
	public List<Parameter> nonOptions;
	private String usageHint = "";
	private String executableName = "";
	
	public CommandLineParser(){
		sortedArgs = new ArrayList<List<String>>();
		options = new ArrayList<Parameter>();
		nonOptions = new ArrayList<Parameter>();
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
	 * @param args - The command line arguments to add. These can be passed straight from the parameter of main(String[])<p>
	 * @return A list of strings, the first([0]) element in each list
	 * 		is the command line option, if the second([1]) element exists it is the
	 * 		parameter for that option.<p>
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

				for (Parameter option : options) {
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
		@SuppressWarnings("unchecked")
		ArrayList<Parameter> options = (ArrayList<Parameter>)this.options.clone();
		String ret = "Usage: " + executableName;
		for(Parameter opt : this.options){
			if(opt.nonOptionParam){
				ret += opt.longOption + " ";
				options.remove(opt);
			}
		}
		ret += "[OPTION]... ";
		ret += "\n" + usageHint + "\n";
		for(Parameter option : options){
			ret += "\t-" + option.option + "\t--" + option.longOption;
			ret += "\n\t\t\t" + option.helpText + "\n";
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
	 * @param optionList a list of Strings of options in a comma separated format<p>
	 * 		single char options should be prepended with a single "-"<p>
	 * 		string options should be prepended with "--"<p>
	 * 		non-option parameters should add a "?" to the string. non-option parameters should define the string (long/--) form option.<p>
	 * 		    ie. parameters that would not have an explicit option before them<p>
	 * 		    eg. cp source.txt dest.txt<p>
	 * 		to add helptext for this option, add -h followed by the help text<p>
	 * 		if there MUST be a parameter after this command line option then add a "+" to the string. 
	 * 		alternatively if there MAY be a parameter after this command line option then add a "*" to the string<p>
	 * 		eg. "-r,--readonly" or "--file,-f,+" or "*, -f, --flag, -h this is helptext" or "--source, ?"
	 * @throws DuplicateParameterException 
	 */
	public void addOptions(String [] optionList) throws DuplicateParameterException{
		for(String option : optionList){
			Parameter f = new Parameter();
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
			addOption(f);;
		}
		System.out.println(options);
	}
	
	/**
	 * Add a single option to this command line parser.
	 * Convenience method for calling addFlags with only one option
	 * @param optionString - formatted string for this command line option, should be in same format as strings in addOptions(String[])
	 * @throws DuplicateParameterException 
	 */
	public void addOption(String optionString) throws DuplicateParameterException{
		addOptions(new String[]{optionString});
	}
	
	
	
	/**
	 * Add a option for this command line parser. Parameters should not be prepended by dashes.
	 * @param shortForm - Single character command line option
	 * @param longForm	- String command line option
	 * @param hasParameter - Should be true if this option will be followed by a parameter when called from the command line.
	 * @throws DuplicateParameterException 
	 */
	public void addOption(char shortForm, String longForm, String helpText, boolean nonOptionParam, boolean hasParameter, boolean parameterRequired) throws DuplicateParameterException{
		Parameter f = new Parameter();
		f.option = ""+shortForm;
		f.longOption = longForm;
		f.hasParam = hasParameter;
		f.paramRequired = parameterRequired;
		f.helpText = helpText;
		f.nonOptionParam = nonOptionParam;
		if(nonOptionParam)
			nonOptions.add(f);
		addOption(f);
	}
	
	/**
	 * Add a option for this command line parser.
	 * @param opt
	 * @throws DuplicateParameterException 
	 */
	public void addOption(Parameter opt) throws DuplicateParameterException{
		duplicateArg(opt);
		options.add(opt);
	}
	
	private void duplicateArg(Parameter opt) throws DuplicateParameterException{
		for(Parameter op : options){
			if(op.option.equals(opt.option))
				throw new DuplicateParameterException(opt.option);
			if(op.longOption.equalsIgnoreCase(opt.longOption))
				throw new DuplicateParameterException(opt.longOption);
		}
	}
	
	/**
	 * Convenience method to add options from an array.
	 * Same as calling addOption(Option) on every element in opts.
	 * @param opts Array of Options to add for this CommandLineParser.
	 * @throws DuplicateParameterException 
	 */
	public void addOptions(Parameter[] opts) throws DuplicateParameterException{
		for(Parameter opt : opts){
			addOption(opt);
		}
	}
	
	private String noDashes(String s){
		return s.replaceFirst("(--|-)", "");
	}
	
	
	public static class Parameter{
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
		
		@Override
		public String toString() {
			return "Parameter [option=" + option + ", longOption=" + longOption
					+ ", param=" + param + ", helpText=" + helpText
					+ ", hasParam=" + hasParam + ", paramRequired="
					+ paramRequired + ", nonOptionParam=" + nonOptionParam
					+ "]";
		}

		
		
	}
	
	public class DuplicateParameterException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public DuplicateParameterException(String parameter) {
			super(parameter.length() > 1 ? "--" : "-" + parameter + " has already been added as an option");
		}
		
	}
	
	@SuppressWarnings("serial")
	public class InvalidFormatException extends RuntimeException{ 
		InvalidFormatException(String s){
			super(s);
		}
	}

}
