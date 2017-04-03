package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImportConfig {

	final Db db;
	final EntityStore store;
	final UpdateMode updateMode;
	final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<ModelType, Set<String>> visited = new HashMap<>();
	private final Consumer<RootEntity> callback;

	private ImportConfig(Db db, EntityStore store, UpdateMode updateMode, Consumer<RootEntity> callback) {
		this.db = db;
		this.store = store;
		this.updateMode = updateMode;
		this.callback = callback;
	}

	static ImportConfig create(Db db, EntityStore store, UpdateMode updateMode, Consumer<RootEntity> callback) {
		return new ImportConfig(db, store, updateMode, callback);
	}

	void visited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		if (set == null)
			visited.put(type, set = new HashSet<>());
		set.add(refId);
	}
	
	void imported(RootEntity entity) {
		if (callback == null)
			return;
		callback.accept(entity);
	}

	boolean hasVisited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		if (set == null)
			return false;
		return set.contains(refId);
	}

}
