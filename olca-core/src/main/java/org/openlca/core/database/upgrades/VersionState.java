package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public enum VersionState {

	UP_TO_DATE,

	NEEDS_UPGRADE,

	HIGHER_VERSION,

	ERROR;

	public static VersionState get(IDatabase db) {
		int v = db.getVersion();
		if (v < 1)
			return ERROR;
		if (v == IDatabase.CURRENT_VERSION)
			return UP_TO_DATE;
		if (v < IDatabase.CURRENT_VERSION)
			return NEEDS_UPGRADE;
		return HIGHER_VERSION;
	}

}
