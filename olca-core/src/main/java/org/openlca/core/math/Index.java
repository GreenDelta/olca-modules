package org.openlca.core.math;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.model.Indexable;

/**
 * Maps a set off n sequentially added objects to an index 0 <= i < n, where the
 * first added value gets the index 0 and the last n-1. The index class is one
 * of the main data structure to map objects to rows and columns in the matrix
 * method.
 */
public class Index<T extends Indexable> {

	private HashMap<String, Integer> map = new HashMap<>();
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
		map.put(item.getRefId(), idx);
		items.add(item);
	}

	public boolean contains(T item) {
		return map.containsKey(item.getRefId());
	}

	/**
	 * Get the index position of the given item. Returns -1 if the item is not
	 * contained in the index.
	 */
	public int getIndex(T item) {
		Integer idx = map.get(item.getRefId());
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
