package org.openlca.io.xls.process.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.io.ImportLog;

record ImportConfig(
		IDatabase db,
		ImportLog log,
		ExchangeProviderQueue providers) {

	static ImportConfig of(IDatabase db) {
		var providers = ExchangeProviderQueue.create(db);
		return new ImportConfig(db, new ImportLog(), providers);
	}
}
