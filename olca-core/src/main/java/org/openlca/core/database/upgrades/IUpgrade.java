package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

/**
 * Updates an openLCA database from a defined version to a newer version.
 */
interface IUpgrade {

	/**
	 * The initial version that the database must have in order to run this
	 * upgrade.
	 */
	int getInitialVersion();

	/**
	 * The version that the database has after the update.
	 */
	int getEndVersion();

	/**
	 * Executes the update on the given database.
	 */
	void exec(IDatabase database) throws Exception;

}
