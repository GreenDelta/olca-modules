package org.openlca.io.xls.process;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.openlca.commons.Strings;
import org.openlca.core.database.Daos;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;

class EntityIndex {

	private final IDatabase db;
	private final ImportLog log;

	private final Map<Class<?>, Map<String, RootEntity>> index = new HashMap<>();
	private final Map<String, Flow> flows = new HashMap<>();
	private final Map<String, String> providerMap = new HashMap<>();

	EntityIndex(IDatabase db, ImportLog log) {
		this.db = db;
		this.log = log;
	}

	<T extends RootEntity> T get(Class<T> type, String name) {
		if (type == null || Strings.isBlank(name))
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

	Flow getFlow(String name, String category) {
		var key = flowKeyOf(name, category);
		var flow = flows.get(key);
		if (flow != null)
			return flow;

		var candidates = new FlowDao(db).getForName(name)
				.stream()
				.filter(f -> flowKeyOf(f).equals(key))
				.toList();

		if (candidates.isEmpty()) {
			log.error("no flow name='" + name
					+ "' category='" + category + "' could be found");
			return null;
		}
		if (candidates.size() > 1) {
			log.warn("there are multiple options for flow name='" + name
					+ "' category='" + category + "' in the database");
		}
		return put(candidates.get(0));
	}

	void addProvider(String name, String refId) {
		providerMap.put(keyOf(name), refId);
	}

	String getProviderId(String name) {
		return Strings.isNotBlank(name)
			? providerMap.get(keyOf(name))
			: null;
	}

	<T extends RootEntity> T sync(Class<T> type, String refId, Supplier<T> fn) {
		var existing = db.get(type, refId);
		if (existing != null) {
			put(existing);
			return existing;
		}
		var e = fn.get();
		return e != null
				? put(db.insert(e))
				: null;
	}

	<T extends RootEntity> T put(T e) {
		if (e == null)
			return null;
		if (e instanceof Flow flow) {
			flows.put(flowKeyOf(flow), flow);
		} else {
			var map = index.computeIfAbsent(e.getClass(), clazz -> new HashMap<>());
			map.put(keyOf(e.name), e);
			if (e instanceof Location loc) {
				if (Strings.isNotBlank(loc.code)) {
					map.put(keyOf(loc.code), loc);
				}
			}
			if (e instanceof Currency currency) {
				if (Strings.isNotBlank(currency.code)) {
					map.put(keyOf(currency.code), currency);
				}
			}
		}
		return e;
	}

	static String keyOf(String label) {
		return label != null
				? label.trim().toLowerCase()
				: "";
	}

	static String flowKeyOf(String name, String category) {
		return keyOf(category) + "/" + keyOf(name);
	}

	static String flowKeyOf(Flow flow) {
		return keyOf(Out.pathOf(flow)) + "/" + keyOf(flow.name);
	}

}
