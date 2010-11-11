package gitcc;

/**
 * This exception is thrown if an attempt is made to create a specific
 * Clearcase implementation but the computer is not configured to support
 * the particular way of accessing ClearCase.
 */
public class ClearcaseEnvironmentNotSetup extends Exception {
	public ClearcaseEnvironmentNotSetup(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
