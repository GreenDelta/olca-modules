package org.openlca.jsonld.input;

import org.openlca.jsonld.EntityStore;

class ImportConfig {

	final Db db;
	final EntityStore store;
	final boolean updateExisting;

	private ImportConfig(Db db, EntityStore store, boolean updateExisting) {
		this.db = db;
		this.store = store;
		this.updateExisting = updateExisting;
	}

	static ImportConfig create(Db db, EntityStore store, boolean updateExisting) {
		return new ImportConfig(db, store, updateExisting);
	}
}
