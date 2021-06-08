package org.openlca.core.database.config;

import java.io.File;

import org.openlca.core.database.IDatabase;

/**
 * A database configuration stores the attributes with which it is possible to
 * create a database connection.
 */
public interface DatabaseConfig {

	/**
	 * Returns a new database instance from this configuration. Note that embedded
	 * databases typically only allow a single instance of a database.
	 *
	 * @param databasesDir the folder where (additional) database files are stored
	 * @return a new database instance created from this configuration
	 */
	IDatabase connect(File databasesDir);

	/**
	 * Returns the name of the database.
	 */
	String name();

	/**
	 * Returns true if this is an embedded database. That means that it runs in
	 * the same JVM process as the respective application.
	 */
	boolean isEmbedded();

}
