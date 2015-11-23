package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Dates;
import org.openlca.jsonld.Schema;

import com.google.gson.JsonObject;

class Writer<T extends RootEntity> {

	JsonObject write(T entity, Consumer<RootEntity> refFn) {
		JsonObject obj = initJson();
		if (entity == null || refFn == null)
			return obj;
		addBasicAttributes(entity, obj);
		if (entity instanceof CategorizedEntity) {
			CategorizedEntity ce = (CategorizedEntity) entity;
			Out.put(obj, "category", ce.getCategory(), refFn);
		}
		return obj;
	}

	private JsonObject initJson() {
		JsonObject object = new JsonObject();
		JsonObject context = new JsonObject();
		Out.put(context, "@vocab", Schema.URI);
		Out.put(context, "@base", Schema.URI);
		JsonObject vocabType = new JsonObject();
		Out.put(vocabType, "@type", "@vocab");
		Out.put(context, "modelType", vocabType);
		Out.put(context, "flowPropertyType", vocabType);
		Out.put(context, "flowType", vocabType);
		Out.put(context, "distributionType", vocabType);
		Out.put(context, "parameterScope", vocabType);
		Out.put(context, "allocationType", vocabType);
		Out.put(context, "defaultAllocationMethod", vocabType);
		Out.put(context, "processType", vocabType);
		Out.put(context, "riskLevel", vocabType);
		Out.put(object, "@context", context);
		return object;
	}

	protected void addBasicAttributes(RootEntity entity, JsonObject obj) {
		String type = entity.getClass().getSimpleName();
		Out.put(obj, "@type", type);
		Out.put(obj, "@id", entity.getRefId());
		Out.put(obj, "name", entity.getName());
		Out.put(obj, "description", entity.getDescription());
		Out.put(obj, "version", Version.asString(entity.getVersion()));
		String lastChange = null;
		if (entity.getLastChange() != 0)
			lastChange = Dates.toString(entity.getLastChange());
		Out.put(obj, "lastChange", lastChange);
	}
	
	boolean isExportExternalFiles() {
		return true;
	}
	
}
