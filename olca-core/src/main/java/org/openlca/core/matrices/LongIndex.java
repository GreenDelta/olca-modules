package org.openlca.core.matrices;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Maps arbitrary values of type long to an ordinal, zero-based index of type
 * int. This class is used to map entity keys to matrix columns. The default
 * value for a key that is not contained in this index is -1.
 */
public class LongIndex {

	private final HashMap<Long, Integer> map = new HashMap<>();
	private final ArrayList<Long> values = new ArrayList<>();

	/**
	 * Adds the given key to the index. The value for the key is the current
	 * size of the index.
	 */
	public int put(long key) {
		if (contains(key))
			return getIndex(key);
		int s = map.size();
		map.put(key, s);
		values.add(key);
		return s;
	}

	public long getKeyAt(int index) {
		return values.get(index);
	}

	/**
	 * Returns the index for the given key. Returns -1 if the key is not
	 * contained in the map.
	 */
	public int getIndex(long key) {
		Integer val = map.get(key);
		if (val == null)
			return -1;
		return val;
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

	/**
	 * Get the keys of this index in their respective order in this index.
	 */
	public long[] getKeys() {
		long[] keys = new long[values.size()];
		for (int i = 0; i < keys.length; i++)
			keys[i] = values.get(i);
		return keys;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

}
