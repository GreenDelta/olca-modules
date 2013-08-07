package org.openlca.core.database;

import org.slf4j.Logger;

/**
 * Unchecked exception for errors while invoking database operations. These
 * exceptions are already logged and are only thrown so interested caller can
 * handle exceptions
 */
public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = 6126554045860754115L;

	static void logAndThrow(Logger log, String message, Throwable e) {
		log.error(message, e);
		throw new DatabaseException(message, e);
	}

	public DatabaseException(String message, Throwable e) {
		super(message, e);
	}

}
