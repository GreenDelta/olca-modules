package org.openlca.core.database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

/**
 * The common interface for openLCA databases.
 */
public interface IDatabase extends Closeable, INotifiable {

	/**
	 * The current database schema version of this package. Together with the
	 * getVersion-method this can be used to check for updates of a database.
	 */
	int CURRENT_VERSION = 9;

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

	/**
	 * Clears the cache of the entity manager of this database. You should
	 * always call this method when you modified the database (via native SQL
	 * queries) outside of the entity manager.
	 */
	default void clearCache() {
		var emf = getEntityFactory();
		if (emf == null)
			return;
		var cache = emf.getCache();
		if (cache != null) {
			cache.evictAll();
		}
	}

	/**
	 * Get the IDs of libraries that are linked to this database.
	 */
	default Set<String> getLibraries() {
		var sql = "select id from tbl_libraries";
		var ids = new HashSet<String>();
		NativeSql.on(this).query(sql, r -> {
			ids.add(r.getString(1));
			return true;
		});
		return ids;
	}

	/**
	 * Registers the library with the given ID to this database. It is the task
	 * of the application layer to resolve the location of the corresponding
	 * library in the file system. Nothing is done if a library with this ID is
	 * already registered.
	 */
	default void addLibrary(String id) {
		var libs = getLibraries();
		if (libs.contains(id))
			return;
		var sql = "insert into tbl_libraries (id) values (?)";
		NativeSql.on(this).update(sql, s -> s.setString(1, id));
	}

	/**
	 * Remove the library with the given ID from this database.
	 */
	default void removeLibrary(String id) {
		var sql = "delete from tbl_libraries where id = ?";
		NativeSql.on(this).update(sql, s -> s.setString(1, id));
	}
}
