package org.openlca.core.matrix.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class CacheUtil {

	private CacheUtil() {
	}

	/**
	 * Converts the given set of 64-bit integers in to a SQL string that can be
	 * used in 'in'-queries; e.g. [1,2,3] is converted to (1,2,3).
	 */
	public static String asSql(Iterable<? extends Long> ids) {
		if (ids == null)
			return "()";
		StringBuilder b = new StringBuilder();
		b.append('(');
		boolean first = true;
		for (Long id : ids) {
			if (!first)
				b.append(',');
			else
				first = false;
			b.append(id);
		}
		b.append(')');
		return b.toString();
	}

	/**
	 * Adds a references to the empty list for keys to the map that are
	 * currently not yet contained in this map.
	 */
	public static <T> void fillEmptyEntries(Iterable<? extends Long> keys,
			Map<Long, List<T>> map) {
		for (Long processId : keys) {
			if (!map.containsKey(processId)) {
				List<T> empty = Collections.emptyList();
				map.put(processId, empty);
			}
		}
	}

	/**
	 * Adds the entry safely to the list for the given key in the map. If there
	 * is no list for this key yet, a new one is created.
	 */
	public static <T> void addListEntry(Map<Long, List<T>> map, T entry,
			Long key) {
		List<T> list = map.get(key);
		if (list == null) {
			list = new ArrayList<>();
			map.put(key, list);
		}
		list.add(entry);
	}

}
