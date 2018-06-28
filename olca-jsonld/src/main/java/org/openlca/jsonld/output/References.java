package org.openlca.jsonld.output;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class References {

	static JsonObject create(RootEntity ref, ExportConfig conf, boolean forceExport) {
		JsonObject obj = create(ref, conf);
		if (obj == null)
			return null;
		ModelType type = ModelType.forModelClass(ref.getClass());
		if (doExportReferences(type, ref.getId(), conf, forceExport))
			conf.refFn.accept(ref);
		return obj;
	}

	static JsonObject create(ModelType type, Long id, ExportConfig conf, boolean forceExport) {
		if (id == null || id == 0)
			return null;
		if (!doExportReferences(type, id, conf, forceExport) || conf.db == null) {
			JsonObject obj = Json.asRef(loadDescriptor(conf.db, type, id), conf.db);
			return obj;
		}
		RootEntity ref = load(conf.db, type, id);
		JsonObject obj = create(ref, conf);
		conf.refFn.accept(ref);
		return obj;
	}

	static JsonObject create(RootEntity ref, ExportConfig conf) {
		return create(Descriptors.toDescriptor(ref), conf);
	}
	
	static JsonObject create(BaseDescriptor descriptor, ExportConfig conf) {
		return Json.asRef(descriptor, conf.db);
	}

	private static boolean doExportReferences(ModelType type, Long id, ExportConfig conf, boolean forceExport) {
		if (conf.hasVisited(type, id))
			return false;
		if (conf.refFn == null)
			return false;
		if (!forceExport && !conf.exportReferences)
			return false;
		if (type == ModelType.UNIT)
			return false;
		return true;
	}

	private static RootEntity load(IDatabase database, ModelType type, long id) {
		return Daos.root(database, type).getForId(id);
	}

	private static BaseDescriptor loadDescriptor(IDatabase database, ModelType type, long id) {
		return Daos.root(database, type).getDescriptor(id);
	}

}
