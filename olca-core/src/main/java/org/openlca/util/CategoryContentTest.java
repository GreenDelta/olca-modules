package org.openlca.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.DataPackages;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import gnu.trove.set.hash.TLongHashSet;
import jakarta.persistence.Table;

/**
 * A utility class for testing if a category has specific contents. It caches
 * the results so that subsequent tests of the same category should be fast.
 */
public class CategoryContentTest {

	private final IDatabase db;
	private final DataPackages dataPackages;
	private final EnumMap<ModelType, Cache> caches = new EnumMap<>(ModelType.class);

	public CategoryContentTest(IDatabase db) {
		this.db = Objects.requireNonNull(db);
		this.dataPackages = db.getDataPackages();
	}

	public void clearCache() {
		caches.clear();
	}

	public void clearCacheOf(ModelType type) {
		if (type != null) {
			caches.remove(type);
		}
	}

	private Cache cacheOf(Category category) {
		return category == null || category.modelType == null
				? Cache.empty()
				: caches.computeIfAbsent(category.modelType, type -> load(db, type));
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements from the given data package.
	 */
	public boolean hasDataPackageContent(Category category, String dataPackage) {
		if (category == null || dataPackage == null)
			return false;
		var cache = cacheOf(category);
		var ids = cache.packages.get(dataPackage);
		if (ids == null)
			return false;
		return test(category, ids);
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements from a data package.
	 */
	public boolean hasDataPackageContent(Category category) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		return test(category, cache.anyPackage);
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements that do not belong to a data package.
	 */
	public boolean hasNonDataPackageContent(Category category) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		return test(category, cache.nonPackage);
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements only from a data package.
	 */
	public boolean hasOnlyDataPackageContent(Category category, String dataPackage) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		var ids = cache.packages.get(dataPackage);
		if (ids == null)
			return false;
		if (!test(category, ids))
			return false;
		if (test(category, cache.nonPackage))
			return false;
		for (var dp : cache.packages.keySet()) {
			if (dp.equals(dataPackage))
				continue;
			if (test(category, cache.packages.get(dp)))
				return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements from a library.
	 */
	public boolean hasLibraryContent(Category category) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		return test(category, cache.anyLib);
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements from the given library.
	 */
	public boolean hasLibraryContent(Category category, String library) {
		if (category == null || library == null)
			return false;
		var cache = cacheOf(category);
		var ids = cache.libs.get(library);
		if (ids == null)
			return false;
		return test(category, ids);
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements that do not belong to a library.
	 */
	public boolean hasNonLibraryContent(Category category) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		return test(category, cache.nonLib);
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements only from a library.
	 */
	public boolean hasOnlyLibraryContent(Category category, String library) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		var ids = cache.libs.get(library);
		if (ids == null)
			return false;
		if (!test(category, ids))
			return false;
		if (test(category, cache.nonLib))
			return false;
		for (var lib : cache.libs.keySet()) {
			if (lib.equals(library))
				continue;
			if (test(category, cache.libs.get(lib)))
				return false;
		}
		return true;
	}

	private boolean test(Category category, TLongHashSet set) {
		if (set.contains(category.id))
			return true;
		for (var child : category.childCategories) {
			if (test(child, set))
				return true;
		}
		return false;
	}

	private Cache load(IDatabase db, ModelType type) {
		if (db == null || type == null)
			return Cache.empty();
		var clazz = type.getModelClass();
		if (clazz == null)
			return Cache.empty();
		var table = clazz.getAnnotation(Table.class);
		if (table == null)
			return Cache.empty();
		var nonPackage = new TLongHashSet();
		var nonLib = new TLongHashSet();
		var anyPackage = new TLongHashSet();
		var anyLib = new TLongHashSet();
		var packages = new HashMap<String, TLongHashSet>();
		var libs = new HashMap<String, TLongHashSet>();
		var sql = "select distinct f_category, data_package from " + table.name();
		NativeSql.on(db).query(sql, r -> {
			var category = r.getLong(1);
			var dataPackage = r.getString(2);
			if (Strings.nullOrEmpty(dataPackage)) {
				nonPackage.add(category);
				nonLib.add(category);
			} else {
				anyPackage.add(category);
				packages.computeIfAbsent(dataPackage, l -> new TLongHashSet())
						.add(category);
				if (!dataPackages.isLibrary(dataPackage)) {
					nonLib.add(category);
				} else {
					anyLib.add(category);
					libs.computeIfAbsent(dataPackage, l -> new TLongHashSet())
							.add(category);
				}
			}
			return true;
		});

		return new Cache(nonPackage, nonLib, anyPackage, anyLib, packages, libs);
	}

	private record Cache(
			TLongHashSet nonPackage,
			TLongHashSet nonLib,
			TLongHashSet anyPackage,
			TLongHashSet anyLib,
			Map<String, TLongHashSet> packages,
			Map<String, TLongHashSet> libs) {

		static Cache empty() {
			return new Cache(
					new TLongHashSet(0),
					new TLongHashSet(0),
					new TLongHashSet(0),
					new TLongHashSet(0),
					Collections.emptyMap(),
					Collections.emptyMap());
		}

	}
}
