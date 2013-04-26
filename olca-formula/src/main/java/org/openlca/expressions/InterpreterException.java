package org.openlca.expressions;

public class InterpreterException extends Exception {

	private static final long serialVersionUID = 8211874342591800345L;

	public InterpreterException() {
	}

	public InterpreterException(String message) {
		super(message);
	}

	public InterpreterException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public InterpreterException(String message, Throwable cause) {
		super(message, cause);
	}

}
