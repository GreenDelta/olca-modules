package org.openlca.core.database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;

import javax.persistence.EntityManagerFactory;

/**
 * The common interface for openLCA databases.
 */
public interface IDatabase extends Closeable, INotifiable {

	/**
	 * The current database schema version of this package. Together with the
	 * getVersion-method this can be used to check for updates of a database.
	 */
	int CURRENT_VERSION = 7;

	/**
	 * Creates a native SQL connection to the underlying database. The
	 * connection should be closed from the respective client.
	 */
	Connection createConnection();

	/**
	 * Returns the entity manager factory from the database.
	 */
	EntityManagerFactory getEntityFactory();

	/**
	 * Returns the database name.
	 */
	String getName();

	int getVersion();

	/**
	 * Get a location where external files that belongs this database are stored
	 * (e.g. PDF or Word documents, shapefiles etc). If there is no such
	 * location for such files for this database, an implementation can just
	 * return null.
	 */
	File getFileStorageLocation();

}
