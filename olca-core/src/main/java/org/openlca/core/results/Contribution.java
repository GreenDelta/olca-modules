package org.openlca.core.results;

import java.util.List;

/**
 * Describes the contribution of an element (flow, LCIA category, process,
 * location, etc.) to a total result.
 */
public class Contribution<T> {

	public T item;

	/**
	 * The absolute amount of the contribution.
	 */
	public double amount;

	/**
	 * The relative share of the contribution.
	 */
	public double share;

	/**
	 * Indicates whether this item is a remainder item of a set of contributions
	 * (e.g. top five and rest). If this is true, the item field is typically null.
	 */
	public boolean isRest = false;

	/**
	 * An optional list of child contributions.
	 */
	public List<Contribution<?>> childs;

	/**
	 * A contribution is a leaf (in a tree) when it has no childs.
	 */
	public boolean isLeaf() {
		return childs == null || childs.isEmpty();
	}
}
