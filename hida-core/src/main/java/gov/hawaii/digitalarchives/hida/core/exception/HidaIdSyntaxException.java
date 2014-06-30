package gov.hawaii.digitalarchives.hida.core.exception;

/**
 * Exception thrown to indicate that a String could not be parsed 
 * as a URI reference. Used where {@link java.net.URISyntaxException} would be used, except this
 * version is unchecked.
 */
public class HidaIdSyntaxException extends HidaException {


	/**
	 * Auto-generated
	 */
	private static final long serialVersionUID = -3767707402158974631L;

	/**
	 * Construct this Id Syntax Exception with the given message.
	 *
	 * @param message The message for this exception.
	 */
	public HidaIdSyntaxException(String message) {
		super(message);
	}
	
	/**
	 * Construct this Id Syntax Exception with the given message, and wrap the
	 * given cause.
	 *
	 * @param message The message for this exception.
	 * @param cause The wrapped exception.
	 */
	public HidaIdSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

}
