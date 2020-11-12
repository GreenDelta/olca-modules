package org.openlca.core.model;

import java.util.UUID;

/**
 * A package private utility class for openLCA models.
 */
class Entities {

	private Entities() {
	}

	/**
	 * Initializes the basic fields (ID, name, etc.) for the given entity.
	 */
	static void init(RootEntity entity, String name) {
		if (entity == null)
			return;
		entity.refId = UUID.randomUUID().toString();
		entity.name = name;
		entity.lastChange = System.currentTimeMillis();
	}

	static void copyRootFields(RootEntity from, RootEntity to) {
		to.refId = (UUID.randomUUID().toString());
		to.name = from.name;
		to.description = from.description;
		to.version = from.version;
		to.lastChange = from.lastChange;
	}

	static void copyFields(CategorizedEntity from, CategorizedEntity to) {
		copyRootFields(from, to);
		to.category = from.category;
		to.tags = from.tags;
		to.library = from.library;
	}

}
