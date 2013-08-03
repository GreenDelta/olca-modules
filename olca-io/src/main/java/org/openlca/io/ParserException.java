package org.openlca.io;

/**
 * Exception occurring in the parsing process
 */
public class ParserException extends Exception {

	private static final long serialVersionUID = -3463855623019771444L;

	/**
	 * Default constructor
	 */
	public ParserException() {
		super();
	}

	/**
	 * Creates a new instance
	 * 
	 * @param message
	 *            The message of the exception
	 */
	public ParserException(final String message) {
		super(message);
	}

	/**
	 * Creates a new instance
	 * 
	 * @param message
	 *            The message of the exception
	 * @param cause
	 *            The cause of the exception
	 */
	public ParserException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new instance
	 * 
	 * @param cause
	 *            The cause of the exception
	 */
	public ParserException(final Throwable cause) {
		super(cause);
	}

}
