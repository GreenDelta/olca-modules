package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;

/**
 * Maps arbitrary values of type long to an ordinal, zero-based index of type
 * int. This class is used to map entity keys to matrix columns. The default
 * value for a key that is not contained in this index is -1.
 */
public class LongIndex {

	private final TLongIntHashMap map = new TLongIntHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			Constants.DEFAULT_LONG_NO_ENTRY_VALUE, // = 0
			-1); // default value for no-index = -1

	private final TLongArrayList values = new TLongArrayList();

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

	/**
	 * Get the keys of this index in their respective order in this index.
	 */
	public long[] getKeys() {
		return values.toArray();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

}
