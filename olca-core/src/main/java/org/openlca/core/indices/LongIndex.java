package org.openlca.core.indices;

import java.util.HashMap;
import java.util.Set;

/**
 * Maps arbitrary values of type long to an ordinal, zero-based index of type
 * int. This class is used to map entity keys to matrix columns. The default
 * value for a key that is not contained in this index is -1.
 */
public class LongIndex {

	private final HashMap<Long, Integer> map = new HashMap<>();

	/**
	 * Adds the given key to the index. The value for the key is the current
	 * size of the index.
	 */
	public int add(long key) {
		int s = map.size();
		map.put(key, s);
		return s;
	}

	/**
	 * Returns the index for the given key. Returns -1 if the key is not
	 * contained in the map.
	 */
	public int get(long key) {
		return map.get(key);
	}

	/**
	 * Returns the number of entries in the index.
	 */
	public int size() {
		return map.size();
	}

	public boolean contains(long key) {
		return map.containsKey(key);
	}

	public long[] getKeys() {
		Set<Long> keySet = map.keySet();
		long[] keys = new long[keySet.size()];
		int i = 0;
		for (long key : keySet) {
			keys[i] = key;
			i++;
		}
		return keys;
	}

}
