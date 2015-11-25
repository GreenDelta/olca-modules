package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.EntityStore;

class ExportConfig {

	final IDatabase db;
	final EntityStore store;
	Consumer<RootEntity> refFn;
	boolean exportReferences = true;
	boolean exportProviders = false;
	private final Map<ModelType, Set<Long>> visited = new HashMap<>();

	private ExportConfig(IDatabase db, EntityStore store) {
		this.db = db;
		this.store = store;
	}

	static ExportConfig create() {
		return new ExportConfig(null, null);
	}

	static ExportConfig create(IDatabase db) {
		return new ExportConfig(db, null);
	}

	static ExportConfig create(IDatabase db, EntityStore store) {
		return new ExportConfig(db, store);
	}

	void visited(RootEntity entity) {
		if (entity == null)
			return;
		ModelType type = ModelType.forModelClass(entity.getClass());
		Set<Long> set = visited.get(type);
		if (set == null)
			visited.put(type, set = new HashSet<>());
		set.add(entity.getId());
	}

	boolean hasVisited(ModelType type, long id) {
		Set<Long> set = visited.get(type);
		if (set == null)
			return false;
		return set.contains(id);
	}

}
