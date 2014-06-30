
package gov.hawaii.digitalarchives.hida.core.model.digitalobject;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

/**
 * Root class for all Exceptions that can be thrown by Digital Object.
 */
public class DigitalObjectException extends HidaException {

	/**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = -5408008564255066921L;

	public DigitalObjectException(String message) {
		super(message);
	}

	public DigitalObjectException(String message, Throwable cause) {
		super(message, cause);
	}
}
