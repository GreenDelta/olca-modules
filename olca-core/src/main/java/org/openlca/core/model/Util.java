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
		to.setRefId((UUID.randomUUID().toString()));
		to.setName(from.getName());
		to.setDescription(from.getDescription());
		to.setVersion(from.getVersion());
		to.setLastChange(from.getLastChange());
	}

}
