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
	final Consumer<RootEntity> refFn;
	boolean exportReferences;
	boolean exportProviders;
	private final Map<ModelType, Set<Long>> visited = new HashMap<>();

	private ExportConfig(IDatabase db, EntityStore store,
			Consumer<RootEntity> refFn) {
		this.db = db;
		this.store = store;
		this.refFn = refFn;
		exportReferences = refFn != null;
	}

	static ExportConfig create() {
		return new ExportConfig(null, null, null);
	}

	static ExportConfig create(IDatabase db, EntityStore store,
			Consumer<RootEntity> refFn) {
		return new ExportConfig(db, store, refFn);
	}

	void visited(ModelType type, long id) {
		Set<Long> set = visited.get(type);
		if (set == null)
			visited.put(type, set = new HashSet<>());
		set.add(id);
	}

	boolean hasVisited(ModelType type, long id) {
		Set<Long> set = visited.get(type);
		if (set == null)
			return false;
		return set.contains(id);
	}

}
