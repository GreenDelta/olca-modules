package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

import com.google.gson.JsonObject;

class References {

	static JsonObject create(RootEntity ref) {
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

	static JsonObject create(RootEntity ref, Consumer<RootEntity> handler) {
		JsonObject obj = create(ref);
		if (obj == null)
			return null;
		if (handler != null)
			handler.accept(ref);
		return obj;
	}

	static JsonObject create(ModelType type, Long id, ExportConfig conf,
			Consumer<RootEntity> handler) {
		if (id == null || id == 0)
			return null;
		boolean doExportReference = !conf.hasVisited(type, id)
				&& handler != null;
		if (!doExportReference) {
			JsonObject obj = create(loadDescriptor(conf.db, type, id));
			return obj;
		}
		RootEntity ref = load(conf.db, type, id);
		JsonObject obj = create(ref);
		handler.accept(ref);
		return obj;
	}

	private static RootEntity load(IDatabase database, ModelType type, long id) {
		Class<?> clazz = type.getModelClass();
		return (RootEntity) database.createDao(clazz).getForId(id);
	}

	private static BaseDescriptor loadDescriptor(IDatabase database,
			ModelType type, long id) {
		Class<?> clazz = type.getModelClass();
		RootEntityDao<?, ?> dao = (RootEntityDao<?, ?>) database
				.createDao(clazz);
		return dao.getDescriptor(id);
	}

}
