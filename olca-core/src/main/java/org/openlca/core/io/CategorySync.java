package org.openlca.core.io;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategorySync {

	private final IDatabase db;
	private final EnumMap<ModelType, Map<String, Category>> cache;

	private CategorySync(IDatabase db) {
		this.db = db;
		cache = new EnumMap<>(ModelType.class);
		for (var category : db.getAll(Category.class)) {
			if (category.modelType == null)
				continue;
			var path = category.toPath();
			if (path == null || path.isEmpty())
				continue;
			var map = cache.computeIfAbsent(
				category.modelType, type -> new HashMap<>());
			map.put(path.toLowerCase(), category);
		}
	}

	public static CategorySync of(IDatabase db) {
		return new CategorySync(db);
	}

	public Category get(ModelType type, String path) {
		if (type == null || path == null || path.isBlank())
			return null;
		var lower = path.toLowerCase();
		var map = cache.computeIfAbsent(type, _type -> new HashMap<>());
		var cached = map.get(lower);
		if (cached != null)
			return cached;

		var dao = new CategoryDao(db);
		var saved = dao.getForPath(type, path);
		if (saved != null) {
			map.put(lower, saved);
			return saved;
		}

		var synced = dao.sync(type, path.split("/"));
		// clear the map because parent categories could
		// be updated too and are then out-of-sync
		map.clear();
		map.put(lower, synced);
		return synced;
	}

	public Category get(ModelType type, String first, String... more) {
		if (type == null)
			return null;
		if (more == null)
			return get(type, first);
		var path = new String[1 + more.length];
		path[0] = first;
		System.arraycopy(more, 0, path, 1, more.length);
		return get(type, String.join("/", path));
	}

}
