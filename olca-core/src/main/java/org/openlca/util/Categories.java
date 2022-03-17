package org.openlca.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Utility functions for openLCA categories.
 */
public class Categories {

	private Categories() {
	}

	public static String createRefId(Category category) {
		if (category == null)
			return null;
		List<String> path = path(category);
		ModelType type = category.modelType;
		if (type != null)
			path.add(0, type.name());
		return KeyGen.get(path.toArray(new String[0]));
	}

	public static List<String> path(Category category) {
		List<String> path = new ArrayList<>();
		Category c = category;
		while (c != null) {
			String item = c.name;
			if (item == null)
				item = "";
			path.add(0, item.trim());
			c = c.category;
		}
		return path;
	}

	public static PathBuilder pathsOf(IDatabase db) {
		return new PathBuilder(db);
	}

	/**
	 * A utility class for building `/` separated category paths for given category
	 * IDs. It manages an internal cache and, thus, is fast when using it for many
	 * requests.
	 */
	public static class PathBuilder {

		private final HashMap<Long, Long> parents = new HashMap<>();
		private final TLongObjectHashMap<String> names = new TLongObjectHashMap<>();
		private final TLongObjectHashMap<String> paths = new TLongObjectHashMap<>();
		private final TLongObjectHashMap<List<String>> lists = new TLongObjectHashMap<>();

		private PathBuilder(IDatabase db) {
			String sql = "select id, name, f_category from tbl_categories";
			try {
				NativeSql.on(db).query(sql, r -> {
					long id = r.getLong(1);
					names.put(id, r.getString(2));
					long parent = r.getLong(3);
					if (!r.wasNull()) {
						parents.put(id, parent);
					}
					return true;
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Returns the category path for the given ID. Passing `null` into this
		 * function is allowed. It will return also `null` in this case as well as
		 * when there is no category with the given ID in the database.
		 */
		public String pathOf(Long categoryId) {
			if (categoryId == null)
				return null;

			// check the cache
			long unboxedId = categoryId;
			var cached = paths.get(unboxedId);
			if (cached != null)
				return cached;

			// build and cache the path
			var path = new StringBuilder();
			long pid = unboxedId;
			while (true) {
				var name = names.get(pid);
				if (name == null)
					break;
				if (path.length() > 0) {
					path.insert(0, '/');
				}
				path.insert(0, name.trim());
				Long parent = parents.get(pid);
				if (parent == null)
					break;
				pid = parent;
			}
			var p = Strings.nullIfEmpty(path.toString());
			paths.put(unboxedId, p);

			return p;
		}

		public List<String> listOf(Long categoryId) {
			if (categoryId == null)
				return Collections.emptyList();

			// check the cache
			long unboxedId = categoryId;
			var cached = lists.get(unboxedId);
			if (cached != null)
				return cached;

			var path = new ArrayList<String>();
			long pid = unboxedId;
			while (true) {
				var name = names.get(pid);
				if (name == null)
					break;
				path.add(0, name);
				var parent = parents.get(pid);
				if (parent == null)
					break;
				pid = parent;
			}

			var list = Collections.unmodifiableList(path);
			lists.put(unboxedId, list);
			return list;
		}
	}
}
