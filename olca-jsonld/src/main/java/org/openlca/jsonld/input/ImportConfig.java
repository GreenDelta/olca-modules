package org.openlca.jsonld.input;

import org.openlca.jsonld.EntityStore;

class ImportConfig {

	final Db db;
	final EntityStore store;
	final UpdateMode updateMode;

	private ImportConfig(Db db, EntityStore store, UpdateMode updateMode) {
		this.db = db;
		this.store = store;
		this.updateMode = updateMode;
	}

	static ImportConfig create(Db db, EntityStore store, UpdateMode updateMode) {
		return new ImportConfig(db, store, updateMode);
	}

}
