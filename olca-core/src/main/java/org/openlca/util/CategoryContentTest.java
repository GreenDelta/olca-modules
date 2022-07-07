package org.openlca.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import gnu.trove.map.hash.TLongByteHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
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

	private final byte TRUE = 1;
	private final byte FALSE = 2;
	private final TLongByteHashMap anyLibContent = new TLongByteHashMap();
	private final TLongByteHashMap nonLibContent = new TLongByteHashMap();
	private final TLongObjectHashMap<Map<String, Byte>> libContent = new TLongObjectHashMap<>();

	public CategoryContentTest(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public void clearCache() {
		anyLibContent.clear();
		nonLibContent.clear();
		libContent.clear();
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements from the given library.
	 */
	public boolean hasLibraryContent(Category category, String library) {
		if (category == null || library == null)
			return false;
		var cache = libContent.get(category.id);
		if (cache != null) {
			var b = cache.get(library);
			if (b != null) {
				return b == TRUE;
			}
		}
		if (cache == null) {
			cache = new HashMap<>();
			libContent.put(category.id, cache);
		}

		var test = ContentTest.forLibrary(db, category.modelType, library);
		if (test == null) {
			cache.put(library, FALSE);
			return false;
		}
		if (test.test(category)) {
			cache.put(library, TRUE);
			return true;
		}

		for (var child : category.childCategories) {
			if (hasLibraryContent(child, library)) {
				cache.put(library, TRUE);
				return true;
			}
		}

		cache.put(library, FALSE);
		return false;
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements from a library.
	 */
	public boolean hasLibraryContent(Category category) {
		return testRecursively(
			category,
			anyLibContent,
			() -> ContentTest.forAnyLibrary(db, category.modelType));
	}

	/**
	 * Returns {@code true} if the given category or a child category of it
	 * contain model elements that do not belong to a library.
	 */
	public boolean hasNonLibraryContent(Category category) {
		return testRecursively(
			category,
			nonLibContent,
			() -> ContentTest.forNonLibrary(db, category.modelType));
	}

	private boolean testRecursively(
		Category category, TLongByteHashMap cache, Supplier<ContentTest> testFn) {
		if (category == null || category.modelType == null)
			return false;
		byte b = cache.get(category.id);
		if (b == TRUE)
			return true;
		if (b == FALSE)
			return false;

		var test = testFn.get();
		if (test == null) {
			cache.put(category.id, FALSE);
			return false;
		}
		if (test.test(category)) {
			cache.put(category.id, TRUE);
			return true;
		}

		for (var child : category.childCategories) {
			if (testRecursively(child, cache, testFn)) {
				cache.put(category.id, TRUE);
				return true;
			}
		}
		cache.put(category.id, FALSE);
		return false;
	}


	private record ContentTest(NativeSql sql, String prefix, String suffix) {

		public static ContentTest forAnyLibrary(IDatabase db, ModelType type) {
			return of(db, type, " and library is not null");
		}

		public static ContentTest forLibrary(
			IDatabase db, ModelType type, String library) {
			return of(db, type, " and library='" + library + "'");
		}

		public static ContentTest forNonLibrary(IDatabase db, ModelType type) {
			return of(db, type, " and library is null");
		}

		private static ContentTest of(IDatabase db, ModelType type, String suffix) {
			var prefix = prefixOf(type);
			if (prefix == null || db == null)
				return null;
			return new ContentTest(NativeSql.on(db), prefix, suffix);
		}

		private static String prefixOf(ModelType type) {
			if (type == null || !type.isRoot())
				return null;
			var modelClass = type.getModelClass();
			if (modelClass == null)
				return null;
			var table = modelClass.getAnnotation(Table.class);
			if (table == null)
				return null;
			return "select count(id) from " + table.name() + " where f_category = ";
		}

		private boolean test(Category category) {
			if (category == null)
				return false;
			var b = new AtomicBoolean(false);
			sql.query(prefix + category.id + suffix, r -> {
				var count = r.getInt(1);
				b.set(count > 0);
				return false;
			});
			return b.get();
		}
	}

}
