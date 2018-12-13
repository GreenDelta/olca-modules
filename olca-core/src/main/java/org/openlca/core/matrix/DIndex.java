package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.openlca.core.model.descriptors.BaseDescriptor;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;

/**
 * Maps a set of descriptors to an ordinal, zero-based index of type int. This
 * class is used to map descriptors to matrix columns. Note that this
 * implementation assumes that the descriptors describe existing objects in a
 * database and thus have an ID > 0. Also, this class is *not* thread safe.
 */
public class DIndex<D extends BaseDescriptor> {

	/**
	 * Contains the ordered content of the index.
	 */
	private final ArrayList<D> content = new ArrayList<>();

	/**
	 * Maps the ID of a descriptor to the position of that descriptor in the
	 * index.
	 */
	private final TLongIntHashMap index = new TLongIntHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			0L, // no entry key
			-1); // no entry value

	/** Returns the number of descriptors in the index. */
	public int size() {
		return content.size();
	}

	/** Returns true if there is no content in this index. */
	public boolean isEmpty() {
		return content.size() == 0;
	}

	/**
	 * Get the descriptor at the given position or null when there is no
	 * descriptor at the given position.
	 */
	public D at(int i) {
		if (i < 0 || i >= content.size())
			return null;
		return content.get(i);
	}

	/**
	 * Get the ID of the descriptor at the given position.
	 */
	public long idAt(int i) {
		D d = at(i);
		return d == null ? 0L : d.getId();
	}

	/**
	 * Returns the position of the given descriptor. If the descriptor is not in
	 * the index, it returns -1.
	 */
	public int of(D d) {
		if (d == null)
			return -1;
		return index.get(d.getId());
	}

	/**
	 * Returns the position of the descriptor with the given ID. If the
	 * descriptor is not in the index, it returns -1.
	 */
	public int of(long id) {
		return index.get(id);
	}

	/**
	 * Returns true when the given descriptor is part of that index.
	 */
	public boolean contains(D d) {
		return of(d) >= 0;
	}

	/**
	 * Returns true when the descriptor with the given ID is part of that index.
	 */
	public boolean contains(long id) {
		return of(id) >= 0;
	}

	/**
	 * Adds the given descriptor to this index if it is not yet contained and
	 * returns the index position of it.
	 */
	public int put(D d) {
		if (d == null)
			return -1;
		int idx = of(d);
		if (idx >= 0)
			return idx;
		idx = content.size();
		content.add(d);
		index.put(d.getId(), idx);
		return idx;
	}

	/**
	 * Adds all descriptors from the given collection to this index.
	 */
	public void putAll(Iterable<D> it) {
		if (it == null)
			return;
		for (D d : it) {
			put(d);
		}
	}

	/**
	 * Get the IDs of all descriptors that are in this index.
	 */
	public long[] ids() {
		return index.keys();
	}

	/**
	 * Calls the given function for each descriptor in this index.
	 */
	public void each(Consumer<D> fn) {
		for (D d : content) {
			fn.accept(d);
		}
	}
}
