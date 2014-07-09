package blackdoor.util;

/**
 * simple object that can be used when verbose output to System.out or
 * System.err is desired. Easily toggled on and off.
 * 
 * @author nfischer3
 * 
 */
public class Verbose {
	private boolean on;
	private boolean showAsError;

	public Verbose(boolean verboseOn, boolean showAsError) {
		on = verboseOn;
		this.showAsError = showAsError;
	}

	public void setVerbosisty(boolean verboseOn) {
		on = verboseOn;
	}

	public void setErrorOutput(boolean showAsError) {
		this.showAsError = showAsError;
	}

	public boolean getVerbosity() {
		return on;
	}

	public void println() {
		if (on)
			System.out.println();
	}

	public void println(String x) {
		if (on)
			if (showAsError)
				System.err.println(x);
			else
				System.out.println(x);
	}

	public void print(String x) {
		if (on)
			if (showAsError)
				System.err.print(x);
			else
				System.out.print(x);
	}
}
