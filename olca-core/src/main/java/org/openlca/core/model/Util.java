package org.openlca.core.model;

import java.util.UUID;

/**
 * A package private utility class for openLCA models.
 */
class Util {

	private Util() {
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
