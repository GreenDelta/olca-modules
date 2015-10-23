package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

class ImportConfig {

	final Db db;
	final EntityStore store;
	final UpdateMode updateMode;
	private final Map<ModelType, Set<String>> visited = new HashMap<>();

	private ImportConfig(Db db, EntityStore store, UpdateMode updateMode) {
		this.db = db;
		this.store = store;
		this.updateMode = updateMode;
	}

	static ImportConfig create(Db db, EntityStore store, UpdateMode updateMode) {
		return new ImportConfig(db, store, updateMode);
	}

	void visited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		if (set == null)
			visited.put(type, set = new HashSet<>());
		set.add(refId);
	}

	boolean hasVisited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		if (set == null)
			return false;
		return set.contains(refId);		
	}

}
