package org.openlca.core.results;

import java.util.List;
import java.util.Objects;

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
	 * The unit of the absolute amount value.
	 */
	public String unit;

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

	public static <T> Contribution<T> of(T item) {
		Contribution<T> c = new Contribution<>();
		c.item = item;
		return c;
	}

	public static <T> Contribution<T> of(T item, double amount) {
		Contribution<T> c = new Contribution<>();
		c.item = item;
		c.amount = amount;
		if (amount != 0) {
			c.share = amount < 0 ? -1 : 1;
		}
		return c;
	}

	/**
	 * A contribution is a leaf (in a tree) when it has no childs.
	 */
	public boolean isLeaf() {
		return childs == null || childs.isEmpty();
	}

	/**
	 * Computes and updates the share of this contribution based on the
	 * given total amount.
	 */
	public void computeShare(double total) {
		if (amount == 0) {
			share = 0;
			return;
		}
		if (total != 0) {
			share = amount / total;
		} else {
			share = amount > 0 ? 1 : -1;
		}
	}

	/**
	 * Calls computeShare on this contribution and its child contributions
	 * recursively with the given total amount.
	 */
	public void computeSharesRecursively(double total) {
		computeShare(total);
		if (isLeaf())
			return;
		for (Contribution<?> child : childs) {
			child.computeSharesRecursively(total);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Contribution<?> other = (Contribution<?>) o;
		return Objects.equals(item, other.item);
	}

	@Override
	public int hashCode() {
		return Objects.hash(item);
	}
}
