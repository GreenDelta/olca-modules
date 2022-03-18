package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.jsonld.JsonStoreWriter;

@Deprecated
class ExportConfig {

	/** The database from which data are exported. */
	final IDatabase db;

	/**
	 * An export specific entity cache which is always present when the export
	 * configuration has a database.
	 */
	final EntityCache cache;

	final JsonStoreWriter store;
	Consumer<RefEntity> refFn;
	boolean exportReferences = true;
	boolean exportProviders = false;
	private final Map<ModelType, Set<Long>> visited = new HashMap<>();

	private ExportConfig(IDatabase db, JsonStoreWriter store) {
		this.db = db;
		this.cache = db != null ? EntityCache.create(db) : null;
		this.store = store;
	}

	static ExportConfig create() {
		return new ExportConfig(null, null);
	}

	static ExportConfig create(IDatabase db) {
		return new ExportConfig(db, null);
	}

	static ExportConfig create(IDatabase db, JsonStoreWriter store) {
		return new ExportConfig(db, store);
	}

	void visited(RefEntity entity) {
		if (entity == null)
			return;
		ModelType type = ModelType.forModelClass(entity.getClass());
		Set<Long> set = visited.computeIfAbsent(type, k -> new HashSet<>());
		set.add(entity.id);
	}

	boolean hasVisited(ModelType type, long id) {
		Set<Long> set = visited.get(type);
		if (set == null)
			return false;
		return set.contains(id);
	}

}
