package org.openlca.core.library;

/**
 * Describes the action that should be performed, when a library is mounted to
 * a database.
 */
public enum MountAction {

	/**
	 * The library is added with all data as new library.
	 */
	ADD,

	/** The library is skipped. */
	SKIP,

	/**
	 * All library data are updated in the database.
	 */
	UPDATE,

	/**
	 * Only the tags of the library data are changed in the database, but
	 * process exchanges and impact factors are may deleted from the database.
	 */
	RETAG

}
