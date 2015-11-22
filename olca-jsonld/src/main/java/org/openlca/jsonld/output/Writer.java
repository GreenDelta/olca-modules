package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Dates;

import com.google.gson.JsonObject;

class Writer<T extends RootEntity> {

	JsonObject write(T entity, Consumer<RootEntity> refHandler) {
		JsonObject obj = initJson();
		if (entity == null || refHandler == null)
			return obj;
		addBasicAttributes(entity, obj);
		if (entity instanceof CategorizedEntity) {
			CategorizedEntity ce = (CategorizedEntity) entity;
			if (ce.getCategory() != null) {
				JsonObject catRef = References.create(ce.getCategory(),
						refHandler);
				obj.add("category", catRef);
			}
		}
		return obj;
	}

	private JsonObject initJson() {
		JsonObject object = new JsonObject();
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", "http://openlca.org/schema/v1.0/");
		context.addProperty("@base", "http://openlca.org/schema/v1.0/");
		JsonObject vocabType = new JsonObject();
		vocabType.addProperty("@type", "@vocab");
		context.add("modelType", vocabType);
		context.add("flowPropertyType", vocabType);
		context.add("flowType", vocabType);
		context.add("distributionType", vocabType);
		context.add("parameterScope", vocabType);
		context.add("allocationType", vocabType);
		context.add("defaultAllocationMethod", vocabType);
		context.add("processType", vocabType);
		context.add("riskLevel", vocabType);
		object.add("@context", context);
		return object;
	}

	protected void addBasicAttributes(RootEntity entity, JsonObject obj) {
		String type = entity.getClass().getSimpleName();
		obj.addProperty("@type", type);
		obj.addProperty("@id", entity.getRefId());
		obj.addProperty("name", entity.getName());
		obj.addProperty("description", entity.getDescription());
		obj.addProperty("version", Version.asString(entity.getVersion()));
		if (entity.getLastChange() != 0) {
			obj.addProperty("lastChange",
					Dates.toString(entity.getLastChange()));
		}
	}
}
