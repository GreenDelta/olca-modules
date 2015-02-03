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
		for (String catId : store.getRefIds(ModelType.CATEGORY))
			CategoryImport.run(catId, store, db);
		for (String groupId : store.getRefIds(ModelType.UNIT_GROUP))
			UnitGroupImport.run(groupId, store, db);
		for (String propId : store.getRefIds(ModelType.FLOW_PROPERTY))
			FlowPropertyImport.run(propId, store, db);
		for (String flowId : store.getRefIds(ModelType.FLOW))
			FlowImport.run(flowId, store, db);
	}

}
