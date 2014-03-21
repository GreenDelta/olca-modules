package org.openlca.ilcd.io;

import java.io.IOException;

public class DataStoreException extends IOException {

	private static final long serialVersionUID = -229298485012228545L;

	public DataStoreException() {
		super();
	}

	public DataStoreException(String message) {
		super(message);
	}

	public DataStoreException(Throwable cause) {
		super(cause);
	}

	public DataStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
