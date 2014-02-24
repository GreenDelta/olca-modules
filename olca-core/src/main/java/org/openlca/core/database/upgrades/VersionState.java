package org.openlca.core.database.upgrades;

/**
 * The result of a version check of a database.
 */
public enum VersionState {

	/**
	 * The database is up to date.
	 */
	CURRENT,

	/**
	 * The database requires updates.
	 */
	OLDER,


	/**
	 * The database is new than the version of openLCA.
	 */
	NEWER,

	/**
	 * Could not get the version because of an error.
	 */
	ERROR

}
