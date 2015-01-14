package org.openlca.jsonld.input;

import org.openlca.core.database.IDatabase;
import org.openlca.jsonld.EntityStore;

public class JsonImport {

	private IDatabase db;
	private EntityStore store;

	public JsonImport(EntityStore store, IDatabase db) {
		this.store = store;
		this.db = db;
	}

}
