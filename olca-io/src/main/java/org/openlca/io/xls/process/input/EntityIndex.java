package org.openlca.io.xls.process.input;

import org.glassfish.jersey.internal.util.Producer;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;
import org.openlca.io.CategoryPath;
import org.openlca.util.Strings;

import java.util.HashMap;
import java.util.Map;

class EntityIndex {

	private final IDatabase db;
	private final ImportLog log;

	private final Map<Class<?>, Map<String, RootEntity>> index = new HashMap<>();
	private final Map<String, Flow> flows = new HashMap<>();

	EntityIndex(IDatabase db, ImportLog log) {
		this.db = db;
		this.log = log;
	}

	<T extends RootEntity> T get(Class<T> type, String name) {
		if (type == null || Strings.nullOrEmpty(name))
			return null;

		// get from index
		var map = index.get(type);
		if (map != null) {
			var e = map.get(keyOf(name));
			if (e != null)
				return type.cast(e);
		}

		// get from database
		var dao = Daos.root(db, type);
		var all = dao.getForName(name);
		if (all.isEmpty()) {
			log.error("No data set '" + name
					+ "' exists (type=" + type.getSimpleName() + ")");
			return null;
		}
		if (all.size() > 1) {
			log.error("Multiple possible data sets for '" + name
					+ "' (type=" + type.getSimpleName() + ")");
		}
		return put(all.get(0));
	}

	<T extends RootEntity> T sync(Class<T> type, String refId, Producer<T> fn) {
		var existing = db.get(type, refId);
		if (existing != null) {
			put(existing);
			return existing;
		}
		var e = fn.call();
		return e != null
				? put(db.insert(e))
				: null;
	}

	private <T extends RootEntity> T put(T e) {
		if (e == null)
			return null;
		if (e instanceof Flow flow) {
			var key = keyOf(CategoryPath.getFull(flow.category))
					+ "/" + keyOf(e.name);
			flows.put(key, flow);
		} else {
			var map = index.computeIfAbsent(e.getClass(), clazz -> new HashMap<>());
			map.put(keyOf(e.name), e);
		}
		return e;
	}

	private String keyOf(String label) {
		return label != null
				? label.trim().toLowerCase()
				: "";
	}

}
