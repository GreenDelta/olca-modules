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
	static void init(RefEntity entity, String name) {
		if (entity == null)
			return;
		entity.refId = UUID.randomUUID().toString();
		entity.name = name;
		if (entity instanceof RootEntity e) {
			e.lastChange = System.currentTimeMillis();
		}
	}

	static void copyRefFields(RefEntity from, RefEntity to) {
		to.refId = (UUID.randomUUID().toString());
		to.name = from.name;
		to.description = from.description;
	}

	static void copyFields(RootEntity from, RootEntity to) {
		copyRefFields(from, to);
		to.category = from.category;
		to.tags = from.tags;
		to.library = from.library;
		to.version = from.version;
		to.lastChange = from.lastChange;
	}

}
