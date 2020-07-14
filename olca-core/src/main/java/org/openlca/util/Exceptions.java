package org.openlca.util;

public final class Exceptions {

	private Exceptions() {
	}

	/**
	 * Wraps the given exception into a runtime exception and throws it.
	 */
	public static void unchecked(String msg, Throwable cause) {
		throw new RuntimeException(msg, cause);
	}

	/**
	 * Wraps the given exception into a runtime exception and throws it.
	 */
	public static void unchecked(Throwable cause) {
		throw new RuntimeException(cause);
	}
}
