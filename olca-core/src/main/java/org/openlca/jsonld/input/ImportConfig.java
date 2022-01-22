package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.JsonStoreReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImportConfig {

	final Db db;
	final JsonStoreReader reader;
	final UpdateMode updateMode;
	final Logger log = LoggerFactory.getLogger(getClass());
	private final ExchangeProviderQueue providers;
	private final Map<ModelType, Set<String>> visited = new HashMap<>();
	private final Consumer<RootEntity> callback;

	private ImportConfig(Db db, JsonStoreReader store,
	                     UpdateMode updateMode, Consumer<RootEntity> callback) {
		this.db = db;
		this.reader = store;
		this.updateMode = updateMode;
		this.callback = callback;
		this.providers = ExchangeProviderQueue.create(db.getDatabase());
	}

	static ImportConfig create(Db db, JsonStoreReader reader,
	                           UpdateMode updateMode, Consumer<RootEntity> callback) {
		return new ImportConfig(db, reader, updateMode, callback);
	}

	void visited(ModelType type, String refId) {
		var set = visited.computeIfAbsent(type, k -> new HashSet<>());
		set.add(refId);
	}

	public ExchangeProviderQueue providers() {
		return providers;
	}

	void imported(RootEntity entity) {
		if (callback == null)
			return;
		callback.accept(entity);
	}

	boolean hasVisited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		return set != null && set.contains(refId);
	}

}
