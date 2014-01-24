package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

/**
 * Updates an openLCA database from a defined version to a newer version.
 */
interface IUpgrade extends Runnable {

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
	 * Initializes the update with the database on which the update should run.
	 */
	void init(IDatabase database);

}
