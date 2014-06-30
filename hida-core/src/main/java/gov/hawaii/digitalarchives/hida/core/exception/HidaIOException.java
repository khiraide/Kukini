
package gov.hawaii.digitalarchives.hida.core.exception;

/**
 * Used to indicate that a exception was caused while performing an IO related
 * task.  Used where {@link java.io.IOException} would be used, except this
 * version is unchecked.
 */
public class HidaIOException extends HidaException {

	/**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = -5625971010064781857L;

	/**
	 * Construct this IO Exception with the given message.
	 *
	 * @param message The message for this exception.
	 */
	public HidaIOException(String message) {
		super(message);
	}

	/**
	 * Construct this IO Exception with the given message, and wrap the
	 * given cause.
	 *
	 * @param message The message for this exception.
	 * @param cause The wrapped exception.
	 */
	public HidaIOException(String message, Throwable cause) {
		super(message, cause);
	}
}
