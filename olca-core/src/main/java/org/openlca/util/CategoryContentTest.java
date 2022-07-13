package org.openlca.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gnu.trove.set.hash.TLongHashSet;
import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

/**
 * A utility class for testing if a category has specific contents. It caches
 * the results so that subsequent tests of the same category should be fast.
 */
public class CategoryContentTest {

	private final IDatabase db;

	private final EnumMap<ModelType, Cache> caches = new EnumMap<>(ModelType.class);

	public CategoryContentTest(IDatabase db) {
		this.db = Objects.requireNonNull(db);
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
			: caches.computeIfAbsent(category.modelType, type -> Cache.load(db, type));
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
	 * contain model elements that do not belong to a library.
	 */
	public boolean hasNonLibraryContent(Category category) {
		if (category == null)
			return false;
		var cache = cacheOf(category);
		return test(category, cache.nonLib);
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

	private record Cache(
		TLongHashSet nonLib,
		TLongHashSet anyLib,
		Map<String, TLongHashSet> libs) {

		static Cache empty() {
			return new Cache(
				new TLongHashSet(0),
				new TLongHashSet(0),
				Collections.emptyMap());
		}

		static Cache load(IDatabase db, ModelType type) {
			if (db == null || type == null || !type.isRoot())
				return empty();
			var clazz = type.getModelClass();
			if (clazz == null)
				return empty();
			var table = clazz.getAnnotation(Table.class);
			if (table == null)
				return empty();

			var nonLib = new TLongHashSet();
			var anyLib = new TLongHashSet();
			var libs = new HashMap<String, TLongHashSet>();
			var sql = "select distinct f_category, library from " + table.name();
			NativeSql.on(db).query(sql, r -> {
				long category = r.getLong(1);
				String lib = r.getString(2);
				if (Strings.nullOrEmpty(lib)) {
					nonLib.add(category);
				} else {
					anyLib.add(category);
					libs.computeIfAbsent(lib, l -> new TLongHashSet())
						.add(category);
				}
				return true;
			});

			return new Cache(nonLib, anyLib, libs);
		}
	}
}
