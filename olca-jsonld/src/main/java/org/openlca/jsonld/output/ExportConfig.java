package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

class ExportConfig {

	final IDatabase db;
	final EntityStore store;
	DefaultProviderOption defaultProviderOption = DefaultProviderOption.EXCLUDE_PROVIDER;
	ProductSystemOption productSystemOption = ProductSystemOption.INCLUDE_PROCESSES;
	private final Map<ModelType, Set<Long>> visited = new HashMap<>();

	private ExportConfig(IDatabase db, EntityStore store) {
		this.db = db;
		this.store = store;
	}

	static ExportConfig create(IDatabase db, EntityStore store) {
		return new ExportConfig(db, store);
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

	static enum DefaultProviderOption {

		/* Export default provider field AND connected process */
		INCLUDE_PROVIDER,

		/* Export default provider field only, don't export process */
		EXCLUDE_PROVIDER;

	}

	static enum ProductSystemOption {

		/* Export reference to process AND connected process */
		INCLUDE_PROCESSES,

		/* Export reference to process only, don't export process */
		EXCLUDE_PROCESSES;

	}

}
