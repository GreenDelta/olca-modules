package org.openlca.jsonld.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.EntityStore;

import com.google.gson.JsonObject;

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

	static void addAttributes(RootEntity entity, JsonObject object,
			EntityStore store) {
		if (entity == null || object == null)
			return;
		String type = entity.getClass().getSimpleName();
		object.addProperty("@type", type);
		object.addProperty("@id", entity.getRefId());
		object.addProperty("name", entity.getName());
		object.addProperty("description", entity.getDescription());
		if (entity instanceof CategorizedEntity)
			addCategory((CategorizedEntity) entity, object, store);
	}

	private static void addCategory(CategorizedEntity entity, JsonObject obj,
			EntityStore store) {
		if (entity == null || obj == null)
			return;
		JsonObject catRef = Out.put(entity.getCategory(), store);
		obj.add("category", catRef);
	}

}
