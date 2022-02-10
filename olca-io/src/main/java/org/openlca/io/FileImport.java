package org.openlca.io;

import org.openlca.core.io.ImportLog;

public interface FileImport extends Runnable {

	/**
	 * Cancels the import.
	 */
	void cancel();

	/**
	 * Returns the import log of this import.
	 */
	ImportLog log();
}
