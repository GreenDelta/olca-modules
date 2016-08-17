package org.openlca.util;

import java.util.HashMap;
import java.util.function.Consumer;

import javax.persistence.Table;

import org.apache.commons.math3.util.Pair;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.RootEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In the imports and exports we often need a mapping of reference IDs to
 * database internal IDs or the other way around. This class exactly provides
 * such mappings by caching all IDs from the database.
 */
public class RefIdMap<From, To> {

	private final HashMap<Class<?>, HashMap<From, To>> map = new HashMap<>();

	@SafeVarargs
	public static <T extends RootEntity> RefIdMap<Long, String> internalToRef(
			IDatabase db, Class<? extends T>... types) {
		RefIdMap<Long, String> refMap = new RefIdMap<>();
		for (Class<?> type : types) {
			HashMap<Long, String> map = new HashMap<>();
			refMap.map.put(type, map);
			scan(db, type, p -> {
				map.put(p.getFirst(), p.getSecond());
			});
		}
		return refMap;
	}

	@SafeVarargs
	public static <T extends RootEntity> RefIdMap<String, Long> refToInternal(
			IDatabase db, Class<? extends T>... types) {
		RefIdMap<String, Long> refMap = new RefIdMap<>();
		for (Class<?> type : types) {
			HashMap<String, Long> map = new HashMap<>();
			refMap.map.put(type, map);
			scan(db, type, p -> {
				map.put(p.getSecond(), p.getFirst());
			});
		}
		return refMap;
	}

	private static void scan(IDatabase db, Class<?> type,
			Consumer<Pair<Long, String>> fn) {
		String table = getTable(type);
		if (table == null)
			return;
		String sql = "select id, ref_id from " + table;
		try {
			NativeSql.on(db).query(sql, r -> {
				long id = r.getLong(1);
				String refId = r.getString(2);
				fn.accept(new Pair<Long, String>(id, refId));
				return true;
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(RefIdMap.class);
			log.error("failed to get database table", e);
		}
	}

	private static String getTable(Class<?> type) {
		try {
			Table table = type.getAnnotation(Table.class);
			return table.name();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(RefIdMap.class);
			log.error("failed to get database table", e);
			return null;
		}
	}

	public To get(Class<?> type, From id) {
		if (type == null || id == null)
			return null;
		HashMap<From, To> m = map.get(type);
		return m == null ? null : m.get(id);
	}
}
