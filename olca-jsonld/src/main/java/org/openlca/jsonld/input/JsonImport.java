package org.openlca.jsonld.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

public class JsonImport implements Runnable {

	private Db db;
	private EntityStore store;

	public JsonImport(EntityStore store, IDatabase db) {
		this.store = store;
		this.db = new Db(db);
	}

	@Override
	public void run() {
		for (String catId : store.getRefIds(ModelType.CATEGORY)) {
		   CategoryImport.run(catId, store, db);
		}
	}

}
