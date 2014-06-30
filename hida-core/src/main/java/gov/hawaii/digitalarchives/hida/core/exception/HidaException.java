
package gov.hawaii.digitalarchives.hida.core.exception;

/**
 * This is the root of the Exception hierarchy for the HiDA Archives System.
 * <p>
 * We believe that the utility of <i>checked</i> exceptions (those that
 * subclass {@link Exception}) are very limited, and all exceptions are better
 * used in a completely unchecked manner.
 */
public class HidaException extends RuntimeException {

	/**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = -3512752283229384211L;
	
	/**
	 * Construct this exception with the given message.
	 * 
	 * @param message The message of the exception.
	 */
	public HidaException(String message) {
		super(message);
	}
	
	/**
	 * Construct the exception with the given message and wrap given nested
	 * exception.
	 * 
	 * @param message The message of the exception.
	 * @param cause The wrapped exception.
	 */
	public HidaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Wrap the given exception in this exception.
	 * 
	 * @param cause The wrapped exception.
	 */
	public HidaException(Throwable cause)  {
		super(cause);
	}
}
