package org.openlca.core.math;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Maps a set off n sequentially added objects to an index 0 <= i < n, where the
 * first added value gets the index 0 and the last n-1. The content type of the
 * index must implement the hash and equals function appropriately.
 */
public class Index<T> {

	private HashMap<T, Integer> map = new HashMap<>();
	private List<T> items = new ArrayList<>();
	private Class<T> contentType;

	public Index(Class<T> contentType) {
		this.contentType = contentType;
	}

	/**
	 * Puts the given into the index. Does nothing if the item is already in
	 * this index.
	 */
	public void put(T item) {
		if (contains(item))
			return;
		int idx = map.size();
		map.put(item, idx);
		items.add(item);
	}

	public boolean contains(T item) {
		return map.containsKey(item);
	}

	/**
	 * Get the index position of the given item. Returns -1 if the item is not
	 * contained in the index.
	 */
	public int getIndex(T item) {
		Integer idx = map.get(item);
		if (idx == null)
			return -1;
		return idx;
	}

	public T getItemAt(int idx) {
		return items.get(idx);
	}

	public int size() {
		return map.size();
	}

	@SuppressWarnings("unchecked")
	public T[] getItems() {
		return items.toArray((T[]) Array.newInstance(contentType, size()));
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}
