package org.openlca.util;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.ProductSystem;

public final class ProductSystems {

	private ProductSystems() {
	}

	/**
	 * Returns true when the given product system has links to library
	 * processes.
	 */
	public static boolean hasLibraryLinks(ProductSystem sys, IDatabase db) {
		if (sys == null || db == null)
			return false;
		if (sys.referenceProcess != null
				&& sys.referenceProcess.isFromLibrary())
			return true;
		var processes = new ProcessDao(db).descriptorMap();
		for (var processID : sys.processes) {
			var process = processes.get(processID);
			if (process != null && process.isFromLibrary())
				return true;
		}
		return false;
	}

}
