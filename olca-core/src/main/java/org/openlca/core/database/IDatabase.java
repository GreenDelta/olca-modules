package org.openlca.core.database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.set.TLongSet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

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
	 * Creates a native SQL connection to the underlying database. The connection
	 * should be closed from the respective client.
	 */
	Connection createConnection();

	/**
	 * Returns the entity manager factory from the database.
	 */
	EntityManagerFactory getEntityFactory();

	/**
	 * Creates a new entity manager which should be closed when it is not needed
	 * anymore.
	 */
	default EntityManager newEntityManager() {
		return getEntityFactory().createEntityManager();
	}

	/**
	 * Returns the database name.
	 */
	String getName();

	int getVersion();

	/**
	 * Get a location where external files that belongs this database are stored
	 * (e.g. PDF or Word documents, shapefiles etc). If there is no such location
	 * for such files for this database, an implementation can just return null.
	 */
	File getFileStorageLocation();

	/**
	 * Clears the cache of the entity manager of this database. You should always
	 * call this method when you modified the database (via native SQL queries)
	 * outside of the entity manager.
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
	 * Returns true if there are libraries linked to this database.
	 */
	default boolean hasLibraries() {
		return getLibraries().size() > 0;
	}

	/**
	 * Registers the library with the given ID to this database. It is the task of
	 * the application layer to resolve the location of the corresponding library in
	 * the file system. Nothing is done if a library with this ID is already
	 * registered.
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

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> T insert(T e) {
		if (e == null)
			return null;
		var dao = (BaseDao<T>) Daos.base(this, e.getClass());
		return dao.insert(e);
	}

	default void insert(AbstractEntity e1, AbstractEntity e2,
											AbstractEntity... more) {
		insert(e1);
		insert(e2);
		if (more == null)
			return;
		for (var e : more) {
			insert(e);
		}
	}

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> T update(T e) {
		var dao = (BaseDao<T>) Daos.base(this, e.getClass());
		return dao.update(e);
	}

	@SuppressWarnings("unchecked")
	default <T extends AbstractEntity> void delete(T e) {
		if (e == null)
			return;
		var dao = (BaseDao<T>) Daos.base(this, e.getClass());
		dao.delete(e);
	}

	default void delete(AbstractEntity e1, AbstractEntity e2,
											AbstractEntity... more) {
		this.delete(e1);
		this.delete(e2);
		if (more == null)
			return;
		for (var e : more) {
			delete(e);
		}
	}

	default <T extends AbstractEntity> T get(Class<T> type, long id) {
		var dao = Daos.base(this, type);
		return dao.getForId(id);
	}

	default <T extends AbstractEntity> List<T> getAll(
		Class<T> type, TLongSet ids) {
		var list = new ArrayList<T>();
		var em = newEntityManager();
		for (var it = ids.iterator(); it.hasNext(); ) {
			var entity = em.find(type, it.next());
			if (entity != null) {
				list.add(entity);
			}
		}
		em.close();
		return list;
	}

	@SuppressWarnings("unchecked")
	default <T extends RootEntity> T get(Class<T> type, String refID) {
		var modelType = ModelType.forModelClass(type);
		if (modelType == null)
			return null;
		var dao = Daos.root(this, modelType);
		return (T) dao.getForRefId(refID);
	}

	/**
	 * Get the descriptor of the entity of the given type and ID.
	 */
	default <T extends RootEntity> Descriptor getDescriptor(
		Class<T> type, long id) {
		var modelType = ModelType.forModelClass(type);
		return modelType == null
			? null
			: Daos.root(this, modelType).getDescriptor(id);
	}

	/**
	 * Get the descriptor of the entity of the given type and reference ID.
	 */
	default <T extends RootEntity> Descriptor getDescriptor(
		Class<T> type, String refID) {
		if (refID == null)
			return null;
		var modelType = ModelType.forModelClass(type);
		return modelType == null
			? null
			: Daos.root(this, modelType).getDescriptorForRefId(refID);
	}

	/**
	 * Get all entities of the given type from this database.
	 */
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> List<T> allOf(Class<T> type) {
		var modelType = ModelType.forModelClass(type);
		if (modelType == null)
			return Collections.emptyList();
		var dao = Daos.root(this, modelType);
		return (List<T>) dao.getAll();
	}

	/**
	 * Get the descriptors of all entities of the given type from this database.
	 */
	default <T extends RootEntity> List<? extends Descriptor> allDescriptorsOf(
		Class<T> type) {
		var modelType = ModelType.forModelClass(type);
		return modelType == null
			? Collections.emptyList()
			: Daos.root(this, modelType).getDescriptors();
	}

	/**
	 * Get the first entity of the given type and with the given name from the
	 * database. It returns `null` if no entity with the given name exists.
	 */
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> T forName(Class<T> type, String name) {
		var modelType = ModelType.forModelClass(type);
		if (modelType == null)
			return null;
		var dao = Daos.root(this, modelType);
		var candidates = dao.getForName(name);
		return candidates.isEmpty()
			? null
			: (T) candidates.get(0);
	}

	/**
	 * Deletes everything from this database. We assume that you now what you
	 * do when calling this method.
	 */
	default void clear() {
		var tables = new ArrayList<String>();
		// type = T means user table
		String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE = 'T'";
		NativeSql.on(this).query(sql, r -> {
			tables.add(r.getString(1));
			return true;
		});
		for (var table : tables) {
			if (table.equalsIgnoreCase("SEQUENCE")
					|| table.equalsIgnoreCase("OPENLCA_VERSION"))
				continue;
			NativeSql.on(this).runUpdate("DELETE FROM " + table);
		}
		NativeSql.on(this).runUpdate("UPDATE SEQUENCE SET SEQ_COUNT = 0");
		this.clearCache();
	}
}
