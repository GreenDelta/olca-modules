package org.openlca.io;

import org.openlca.core.io.Cancelable;
import org.openlca.core.io.ImportLog;

public interface Import extends Cancelable {

	/**
	 * Returns the import log of this import.
	 */
	ImportLog log();

}
