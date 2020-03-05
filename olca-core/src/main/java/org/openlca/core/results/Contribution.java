package org.openlca.core.results;

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

}
