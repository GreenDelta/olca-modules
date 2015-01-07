package org.openlca.jsonld.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.EntityStore;

import com.google.gson.JsonObject;

public class JsonWriter {

	private final EntityStore store;

	public JsonWriter(EntityStore store) {
		this.store = store;
	}

	public void write(RootEntity entity, IDatabase database) {
		if (entity == null)
			return;
		Refs.put(entity, store);
	}

	static void addContext(JsonObject object) {
		String url = "http://openlca.org/schema/v1.0#";
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", url);
		JsonObject vocabType = new JsonObject();
		vocabType.addProperty("@type", "@vocab");
		context.add("modelType", vocabType);
		context.add("flowPropertyType", vocabType);
		context.add("flowType", vocabType);
		context.add("distributionType", vocabType);
		context.add("parameterScope", vocabType);
		context.add("allocationType", vocabType);
		context.add("defaultAllocationMethod", vocabType);
		context.add("processTyp", vocabType);
		object.add("@context", context);
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
		JsonObject catRef = Refs.put(entity.getCategory(), store);
		obj.add("category", catRef);
	}

}
