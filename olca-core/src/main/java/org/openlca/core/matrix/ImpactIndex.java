package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps a set of impact categories to a matrix index. Note that the index
 * maps the IDs of the impacts so that each impact category needs a unique
 * ID > 0 (which is the case when it is stored in a database).
 */
public class ImpactIndex {

	/**
	 * Contains the ordered content of the index.
	 */
	private final ArrayList<ImpactDescriptor> content = new ArrayList<>();

	/**
	 * Maps the ID of an impact to the position of that impact in the
	 * index.
	 */
	private final TLongIntHashMap index = new TLongIntHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			0L, // no entry key
			-1); // no entry value

	public static ImpactIndex of(Iterable<ImpactDescriptor> impacts) {
		var index = new ImpactIndex();
		if (impacts == null)
			return index;
		for (var impact : impacts) {
			index.put(impact);
		}
		return index;
	}

	public static ImpactIndex of(IDatabase db, ImpactMethodDescriptor method) {
		if (db == null || method == null)
			return empty();
		var impacts = new ImpactMethodDao(db)
			.getCategoryDescriptors(method.id);
		return of(impacts);
	}

	public static ImpactIndex of(IDatabase db) {
		if (db == null)
			return empty();
		var impacts = new ImpactCategoryDao(db)
			.getDescriptors();
		return of(impacts);
	}

	public static ImpactIndex empty() {
		return new ImpactIndex();
	}

	/**
	 * Returns the number of impact categories in the index.
	 */
	public int size() {
		return content.size();
	}

	/**
	 * Returns true if there is no content in this index.
	 */
	public boolean isEmpty() {
		return content.size() == 0;
	}

	/**
	 * Get the impact category at the given position or null when
	 * there is no impact category mapped to the given position.
	 */
	public ImpactDescriptor at(int i) {
		if (i < 0 || i >= content.size())
			return null;
		return content.get(i);
	}

	/**
	 * Get the ID of the impact category at the given position.
	 */
	public long idAt(int i) {
		var d = at(i);
		return d == null ? 0L : d.id;
	}

	/**
	 * Returns the position of the given impact category. If the impact
	 * category is not contained in this index, it returns -1.
	 */
	public int of(ImpactDescriptor d) {
		if (d == null)
			return -1;
		return index.get(d.id);
	}

	/**
	 * Returns the position of the impact category with the given ID. If the
	 * impact category is not contained in this index, it returns -1.
	 */
	public int of(long id) {
		return index.get(id);
	}

	/**
	 * Returns true when the given impact category is part of this index.
	 */
	public boolean contains(ImpactDescriptor d) {
		return of(d) >= 0;
	}

	/**
	 * Returns true when the impact category with the given ID is part
	 * of this index.
	 */
	public boolean contains(long id) {
		return of(id) >= 0;
	}

	/**
	 * Adds the given impact category to this index if it is not yet
	 * contained and returns the index position of it.
	 */
	public int put(ImpactDescriptor d) {
		if (d == null)
			return -1;
		int idx = of(d);
		if (idx >= 0)
			return idx;
		idx = content.size();
		content.add(d);
		index.put(d.id, idx);
		return idx;
	}

	/**
	 * Adds all impact categories from the given collection to this index.
	 */
	public void putAll(Iterable<ImpactDescriptor> it) {
		if (it == null)
			return;
		for (var d : it) {
			put(d);
		}
	}

	/**
	 * Get the (unordered) IDs of all impact categories that are in this index.
	 */
	public long[] ids() {
		return index.keys();
	}

	/**
	 * Calls the given function for each impact category in this index.
	 */
	public void each(IndexConsumer<ImpactDescriptor> fn) {
		for (int i = 0; i < content.size(); i++) {
			fn.accept(i, content.get(i));
		}
	}

	/**
	 * Returns the impact categories of this index.
	 */
	public Set<ImpactDescriptor> content() {
		return new HashSet<>(content);
	}
}
