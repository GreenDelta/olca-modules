package org.openlca.core.model;

import java.util.UUID;

/**
 * A package private utility class for openLCA models.
 */
class Util {

	private Util() {
	}

	static void cloneRootFields(RootEntity from, RootEntity to) {
		if (from == null || to == null)
			return;
		to.refId = (UUID.randomUUID().toString());
		to.name = from.name;
		to.description = from.description;
		to.version = from.version;
		to.lastChange = from.lastChange;
	}

}
