package org.openlca.core.database;

import java.io.Closeable;
import java.sql.Connection;

import javax.persistence.EntityManagerFactory;

/**
 * The common interface for openLCA databases.
 */
public interface IDatabase extends Closeable {

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
	 * Creates a new DAO for the given class. DAOs support the standard
	 * operations like insert, update, or delete. Alternatively, specific DAOs
	 * can be created using the entity manager factory from this class.
	 */
	<T> BaseDao<T> createDao(Class<T> clazz);

	/**
	 * Returns the database name.
	 */
	public String getName();

}
