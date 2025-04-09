package org.openlca.core.database;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.store.EntityStore;
import org.openlca.util.Strings;
import org.openlca.util.TLongSets;

import gnu.trove.set.TLongSet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * The common interface for openLCA databases.
 */
public interface IDatabase extends EntityStore, Closeable {

	/**
	 * The current database schema version of this package. Together with the
	 * getVersion-method this can be used to check for updates of a database.
	 */
	int CURRENT_VERSION = 15;

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
	 * (e.g. PDF or Word documents, shapefiles etc). If there is no such
	 * location for such files for this database, an implementation can just
	 * return null.
	 */
	File getFileStorageLocation();

	/**
	 * Clears the cache of the entity manager of this database. You should
	 * always call this method when you modified the database (via native SQL
	 * queries) outside the entity manager.
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
	 * Registers the library with the given ID to this database. It is the task
	 * of the application layer to resolve the location of the corresponding
	 * library in the file system. Nothing is done if a library with this ID is
	 * already registered.
	 */
	default void addLibrary(String name) {
		var dataPackage = getDataPackage(name);
		if (dataPackage != null) {
			if (dataPackage.isLibrary)
				return;
			throw new IllegalStateException(
					"There is already a non-library data package with the name " + name + " registered");
		}
		NativeSql.on(this).runUpdate(
				"insert into tbl_data_packages(name, version, is_library) "
						+ "values ('" + name + "', 0, 1)");
	}

	default DataPackage getDataPackage(String name) {
		var sql = "select name, url, is_library from tbl_data_packages where name = '" + name + "'";
		var packages = new ArrayList<DataPackage>();
		NativeSql.on(this).query(sql, r -> {
			packages.add(new DataPackage(
					r.getString(1),
					r.getString(2),
					r.getBoolean(3)));
			return true;
		});
		if (packages.isEmpty())
			return null;
		return packages.get(0);
	}

	default DataPackages getDataPackages() {
		var sql = "select name, url, is_library from tbl_data_packages";
		var packages = new HashSet<DataPackage>();
		NativeSql.on(this).query(sql, r -> {
			packages.add(new DataPackage(
					r.getString(1),
					r.getString(2),
					r.getBoolean(3)));
			return true;
		});
		return new DataPackages(packages);
	}

	/**
	 * Remove the data package with the given name from this database.
	 */
	default void removeDataPackage(String name) {
		NativeSql.on(this).runUpdate("delete from tbl_data_packages where name = '" + name + "'");
	}

	/**
	 * Add data package with the given name from this database.
	 */
	default void addDataPackage(String name) {
		var dataPackage = getDataPackage(name);
		if (dataPackage != null) {
			throw new IllegalStateException(
					"There is already a data package with the name " + name + " registered");
		}
		NativeSql.on(this).runUpdate(
				"insert into tbl_data_packages(name, is_library) values ('" + name + "', 0)");
	}

	@Override
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> T insert(T e) {
		if (e == null)
			return null;
		var dao = (BaseDao<T>) Daos.root(this, e.getClass());
		return dao.insert(e);
	}

	@Override
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> T update(T e) {
		var dao = (BaseDao<T>) Daos.root(this, e.getClass());
		return dao.update(e);
	}

	@Override
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> void delete(T e) {
		if (e == null)
			return;
		var dao = (BaseDao<T>) Daos.root(this, e.getClass());
		dao.delete(e);
	}

	@Override
	default <T extends RootEntity> T get(Class<T> type, long id) {
		var dao = Daos.base(this, type);
		return dao.getForId(id);
	}

	@Override
	default <T extends RootEntity> List<T> getAll(Class<T> type, TLongSet ids) {
		var list = new ArrayList<T>();
		var em = newEntityManager();
		for (var it = ids.iterator(); it.hasNext();) {
			var entity = em.find(type, it.next());
			if (entity != null) {
				list.add(entity);
			}
		}
		em.close();
		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> T get(Class<T> type, String refId) {
		if (type == null || refId == null)
			return null;
		var modelType = ModelType.of(type);
		if (modelType == null)
			return null;
		var dao = Daos.root(this, modelType);
		return dao == null
				? null
				: (T) dao.getForRefId(refId);
	}

	/**
	 * Get the descriptor of the entity of the given type and ID.
	 */
	@Override
	default <T extends RootEntity> RootDescriptor getDescriptor(
			Class<T> type, long id) {
		var modelType = ModelType.of(type);
		var dao = Daos.root(this, modelType);
		return dao == null
				? null
				: dao.getDescriptor(id);
	}

	@Override
	default <T extends RootEntity> RootDescriptor getDescriptor(
			Class<T> type, String refID) {
		if (refID == null)
			return null;
		var modelType = ModelType.of(type);
		var dao = Daos.root(this, modelType);
		return dao == null
				? null
				: dao.getDescriptorForRefId(refID);
	}

	@Override
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> List<T> getAll(Class<T> type) {
		var modelType = ModelType.of(type);
		if (modelType == null)
			return Collections.emptyList();
		var dao = Daos.root(this, modelType);
		return dao == null
				? Collections.emptyList()
				: (List<T>) dao.getAll();
	}

	@Override
	default <T extends RootEntity> List<? extends RootDescriptor> getDescriptors(
			Class<T> type) {
		var modelType = ModelType.of(type);
		var dao = Daos.root(this, modelType);
		return dao == null
				? Collections.emptyList()
				: dao.getDescriptors();
	}

	default <T extends RootEntity> List<? extends RootDescriptor> getDescriptors(
			Class<T> type, Set<Long> ids) {
		if (type == null || ids.isEmpty())
			return Collections.emptyList();
		var modelType = ModelType.of(type);
		var dao = Daos.root(this, modelType);
		return dao != null
				? dao.getDescriptors(ids)
				: Collections.emptyList();
	}

	@Override
	default <T extends RootEntity> List<? extends RootDescriptor> getDescriptors(
			Class<T> type, TLongSet ids) {
		if (type == null || ids.isEmpty())
			return Collections.emptyList();
		var modelType = ModelType.of(type);
		var dao = Daos.root(this, modelType);
		return dao != null
				? dao.getDescriptors(TLongSets.box(ids)) // TODO: not very
															// efficient
				: Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	default <T extends RootEntity> T getForName(Class<T> type, String name) {
		var modelType = ModelType.of(type);
		if (modelType == null)
			return null;
		var dao = Daos.root(this, modelType);
		if (dao == null)
			return null;
		var candidates = dao.getForName(name);
		return candidates.isEmpty()
				? null
				: (T) candidates.get(0);
	}

	@Override
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
		this.clearCache();
	}

	/**
	 * Executes the given function in a transaction. It closes the provided
	 * entity manager when the function is done. When the function fails with an
	 * exception the transaction is rolled back.
	 *
	 * @param fn
	 *            the function that should be executed within a transaction
	 */
	default void transaction(Consumer<EntityManager> fn) {
		var em = newEntityManager();
		var transaction = em.getTransaction();
		transaction.begin();
		try {
			fn.accept(em);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw new RuntimeException("failed to execute transaction", e);
		} finally {
			em.close();
		}
	}

	public record DataPackage(String name, String url, boolean isLibrary) {

		public static DataPackage library(String name, String url) {
			return new DataPackage(name, url, true);
		}

		@Override
		public final boolean equals(Object o) {
			if (!(o instanceof DataPackage p))
				return false;
			return name.equals(p.name);
		}

		@Override
		public final int hashCode() {
			return name.hashCode();
		}

	}

	public static class DataPackages {

		private final Map<String, DataPackage> packages;

		public DataPackages() {
			this.packages = new HashMap<>();
		}

		public DataPackages(Set<DataPackage> dataPackages) {
			this.packages = dataPackages.stream()
					.collect(Collectors.toMap(
							p -> p.name,
							p -> p));
		}

		public DataPackage get(String name) {
			return packages.get(name);
		}

		public boolean contains(String name) {
			return get(name) != null;
		}

		public boolean isEmpty() {
			return packages.isEmpty();
		}

		public boolean isFromLibrary(Descriptor d) {
			return d != null && isLibrary(d.dataPackage);
		}

		public boolean isFromLibrary(RootEntity e) {
			return e != null && isLibrary(e.dataPackage);
		}

		public boolean isLibrary(String name) {
			if (Strings.nullOrEmpty(name))
				return false;
			var p = get(name);
			// check defensive, if package is not found assume its a library
			if (p == null)
				return true;
			return p.isLibrary;
		}

		public Set<DataPackage> getAll() {
			return new HashSet<>(packages.values());
		}

		public Set<String> getLibraries() {
			return packages.values().stream()
					.filter(DataPackage::isLibrary)
					.map(DataPackage::name)
					.collect(Collectors.toSet());
		}

	}

}
