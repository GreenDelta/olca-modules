package org.openlca.jsonld.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.EntityStore;

/**
 * Writes entities to an entity store (e.g. a document or zip file). It also
 * writes the referenced entities to this store if they are not yet contained.
 */
public class JsonExport {

	private final EntityStore store;

	public JsonExport(IDatabase database, EntityStore store) {
		this.store = store;
	}

	public void write(RootEntity entity) {
		if (entity == null)
			return;
		Out.put(entity, store);
	}
}
