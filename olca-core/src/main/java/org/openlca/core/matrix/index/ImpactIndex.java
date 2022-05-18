package org.openlca.core.matrix.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;

/**
 * Maps a set of impact categories to a matrix index. Note that the index
 * maps the IDs of the impacts so that each impact category needs a unique
 * ID > 0 (which is the case when it is stored in a database).
 */
public class ImpactIndex implements MatrixIndex<ImpactDescriptor> {

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
		if (impacts == null)
			return empty();
		var index = new ImpactIndex();
		for (var impact : impacts) {
			index.add(impact);
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

	public static ImpactIndex of(ImpactMethod method) {
		if (method == null)
			return empty();
		var index = new ImpactIndex();
		for (var impact : method.impactCategories) {
			index.add(Descriptor.of(impact));
		}
		return index;
	}

	public static ImpactIndex empty() {
		return new ImpactIndex();
	}

	/**
	 * Returns the number of impact categories in the index.
	 */
	@Override
	public int size() {
		return content.size();
	}

	/**
	 * Returns true if there is no content in this index.
	 */
	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}

	/**
	 * Get the impact category at the given position or null when
	 * there is no impact category mapped to the given position.
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
	public int add(ImpactDescriptor d) {
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
	 * Calls the given function for each impact category in this index.
	 */
	@Override
	public void each(IndexConsumer<ImpactDescriptor> fn) {
		for (int i = 0; i < content.size(); i++) {
			fn.accept(i, content.get(i));
		}
	}

	/**
	 * Returns the impact categories of this index.
	 */
	@Override
	public Set<ImpactDescriptor> content() {
		return new HashSet<>(content);
	}

	@Override
	public MatrixIndex<ImpactDescriptor> copy() {
		var copy = new ImpactIndex();
		for (var impact : content) {
			copy.add(impact);
		}
		return copy;
	}

	@Override
	public Iterator<ImpactDescriptor> iterator() {
		return Collections.unmodifiableList(content).iterator();
	}

	public ImpactDescriptor getForId(long id) {
		int pos = of(id);
		return pos >= 0
			? at(pos)
			: null;
	}
}
