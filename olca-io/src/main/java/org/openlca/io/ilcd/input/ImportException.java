package org.openlca.io.ilcd.input;

public class ImportException extends Exception {

	private static final long serialVersionUID = 4017977542051592924L;

	public ImportException() {
		super();
	}

	public ImportException(String message) {
		super(message);
	}

	public ImportException(Throwable cause) {
		super(cause);
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}

}
