package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class Contributions {

	private Contributions() {
	}

	public static <T> Contribution<T> get(
			List<Contribution<T>> contributions, T item) {
		if (contributions == null)
			return null;
		for (Contribution<T> c : contributions) {
			if (Objects.equals(c.item, item))
				return c;
		}
		return null;
	}

	/**
	 * Calculates a contribution set of the given collection of items to the
	 * given total amount. The contribution values and shares are calculated
	 * with the given amount functions which maps an item to the respective
	 * contribution amount of this item: <br>
	 * <code>
	 * contributionSet = Contributions.calculate(items, item -> amount)
	 * </code> The share of the contribution item is calculated via: <br>
	 * <code>
	 * share = item -> amount / abs(totalAmount)
	 * </code> An contribution item is set as the "rest" item
	 * (contributionItem.isRest = true) if the item in the collection is null).
	 */
	public static <T> List<Contribution<T>> calculate(Collection<T> items,
			double totalAmount, ToDoubleFunction<T> fn) {
		List<Contribution<T>> contributions = new ArrayList<>();
		double total = Math.abs(totalAmount);
		for (T item : items) {
			Contribution<T> contribution = new Contribution<>();
			contribution.isRest = item == null;
			contribution.item = item;
			double val = fn.applyAsDouble(item);
			contribution.amount = val;
			if (total != 0)
				contribution.share = val / total;
			contributions.add(contribution);
		}
		return contributions;
	}

	public static <T> List<Contribution<T>> calculate(Collection<T> items,
			ToDoubleFunction<T> fn) {
		List<Contribution<T>> contributions = new ArrayList<>();
		for (T item : items) {
			Contribution<T> contribution = new Contribution<>();
			contribution.isRest = item == null;
			contribution.item = item;
			contribution.amount = fn.applyAsDouble(item);
			contributions.add(contribution);
		}
		calculateShares(contributions);
		return contributions;
	}

	/**
	 * Calculates the relative shares of the given contribution items.
	 */
	public static void calculateShares(
			List<? extends Contribution<?>> contributions) {
		if (contributions == null || contributions.isEmpty())
			return;
		double refVal = Math.abs(getRefValue(contributions));
		for (Contribution<?> c : contributions) {
			if (refVal == 0)
				c.share = 0;
			else
				c.share = c.amount / refVal;
		}
	}

	private static double getRefValue(
			List<? extends Contribution<?>> contributions) {
		Contribution<?> first = contributions.get(0);
		double max = first.amount;
		double min = max;
		for (int i = 1; i < contributions.size(); i++) {
			Contribution<?> next = contributions.get(i);
			double nextVal = next.amount;
			max = Math.max(max, nextVal);
			min = Math.min(min, nextVal);
		}
		return Math.max(Math.abs(max), Math.abs(min));
	}

	public static <T> void sortAscending(List<Contribution<T>> items) {
		items.sort(new Sorter(true));
	}

	public static <T> void sortDescending(List<Contribution<T>> items) {
		items.sort(new Sorter(false));
	}

	/**
	 * Returns the top-contributors of the given list ordered by their
	 * contribution values in descending order. If there are more items than the
	 * given number (maxItems) a rest-item is created at the bottom of the list
	 * which gets the sum of the items not in the list. Thus the returned list
	 * has <code>maxItems</code> entries.
	 */
	public static <T> List<Contribution<T>> topWithRest(
			List<Contribution<T>> items, int maxItems) {
		if (items == null)
			return Collections.emptyList();
		sortDescending(items);
		if (items.size() <= maxItems)
			return items;
		List<Contribution<T>> list = new ArrayList<>();
		Contribution<T> restItem = new Contribution<>();
		restItem.isRest = true;
		for (int i = 0; i < items.size(); i++) {
			Contribution<T> item = items.get(i);
			if (i < (maxItems - 1))
				list.add(item);
			else {
				restItem.amount = restItem.amount + item.amount;
				restItem.share = restItem.share + item.share;
			}
		}
		list.add(restItem);
		return list;
	}

	private static class Sorter implements Comparator<Contribution<?>> {

		private final boolean ascending;

		public Sorter(boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public int compare(Contribution<?> o1, Contribution<?> o2) {
			if (o1 == null || o2 == null)
				return 0;
			if (o1.isRest)
				return 1;
			if (o2.isRest)
				return -1;
			if (ascending)
				return Double.compare(o1.amount, o2.amount);
			else
				return -Double.compare(o1.amount, o2.amount);
		}
	}
}
