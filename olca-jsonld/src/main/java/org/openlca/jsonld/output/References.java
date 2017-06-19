package org.openlca.jsonld.output;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

import com.google.gson.JsonObject;

class References {

	static JsonObject create(RootEntity ref, ExportConfig conf,
			boolean forceExport) {
		JsonObject obj = create(ref);
		if (obj == null)
			return null;
		ModelType type = ModelType.forModelClass(ref.getClass());
		if (doExportReferences(type, ref.getId(), conf, forceExport))
			conf.refFn.accept(ref);
		return obj;
	}

	static JsonObject create(ModelType type, Long id, ExportConfig conf,
			boolean forceExport) {
		if (id == null || id == 0)
			return null;
		if (!doExportReferences(type, id, conf, forceExport) || conf.db == null) {
			JsonObject obj = create(loadDescriptor(conf.db, type, id));
			return obj;
		}
		RootEntity ref = load(conf.db, type, id);
		JsonObject obj = create(ref);
		conf.refFn.accept(ref);
		return obj;
	}

	private static JsonObject create(RootEntity ref) {
		return create(Descriptors.toDescriptor(ref));
	}

	static JsonObject create(BaseDescriptor ref) {
		if (ref == null)
			return null;
		JsonObject obj = new JsonObject();
		String type = ref.getModelType().getModelClass().getSimpleName();
		Out.put(obj, "@type", type);
		Out.put(obj, "@id", ref.getRefId());
		Out.put(obj, "name", ref.getName());
		return obj;
	}

	private static boolean doExportReferences(ModelType type, Long id,
			ExportConfig conf, boolean forceExport) {
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
		Class<? extends AbstractEntity> clazz = type.getModelClass();
		return (RootEntity) database.createDao(clazz).getForId(id);
	}

	private static BaseDescriptor loadDescriptor(IDatabase database,
			ModelType type, long id) {
		return Daos.createRootDao(database, type).getDescriptor(id);
	}

}
