package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

/**
 * Updates an openLCA database from one or more defined versions to a newer
 * version.
 */
interface IUpgrade {

	/**
	 * The initial versions for which this upgrade is valid. The upgrade must
	 * run safely with all of these database versions and produce an identical
	 * database schema for all these versions.
	 */
	int[] getInitialVersions();

	/**
	 * The version that the database has after the update.
	 */
	int getEndVersion();

	/**
	 * Executes the update on the given database.
	 */
	void exec(IDatabase database);

}
