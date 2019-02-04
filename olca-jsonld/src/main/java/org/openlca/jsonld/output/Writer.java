package org.openlca.jsonld.output;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Dates;
import org.openlca.jsonld.Schema;

import com.google.gson.JsonObject;

class Writer<T extends RootEntity> {

	ExportConfig conf;

	protected Writer(ExportConfig conf) {
		this.conf = conf;
	}

	JsonObject write(T entity) {
		JsonObject obj = initJson();
		// mark entity directly as visited to avoid endless cyclic exports for
		// cyclic references
		addBasicAttributes(entity, obj);
		conf.visited(entity);
		if (entity == null)
			return obj;
		if (entity instanceof CategorizedEntity) {
			CategorizedEntity ce = (CategorizedEntity) entity;
			Out.put(obj, "category", ce.category, conf);
		}
		return obj;
	}

	static JsonObject initJson() {
		JsonObject object = new JsonObject();
		Out.put(object, "@context", Schema.CONTEXT_URI);
		return object;
	}

	static void addBasicAttributes(RootEntity entity, JsonObject obj) {
		String type = entity.getClass().getSimpleName();
		Out.put(obj, "@type", type);
		Out.put(obj, "@id", entity.refId, Out.REQUIRED_FIELD);
		Out.put(obj, "name", entity.name);
		Out.put(obj, "description", entity.description);
		Out.put(obj, "version", Version.asString(entity.version));
		String lastChange = null;
		if (entity.lastChange != 0)
			lastChange = Dates.toString(entity.lastChange);
		Out.put(obj, "lastChange", lastChange);
	}

	boolean isExportExternalFiles() {
		return true;
	}

}
