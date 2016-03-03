package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Contributions {

	private Contributions() {
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
	public static <T> ContributionSet<T> calculate(Collection<T> items,
			double totalAmount, Function<T> fn) {
		List<ContributionItem<T>> contributions = new ArrayList<>();
		double total = Math.abs(totalAmount);
		for (T item : items) {
			ContributionItem<T> contribution = new ContributionItem<>();
			contribution.rest = item == null;
			contribution.item = item;
			double val = fn.value(item);
			contribution.amount = val;
			if (total != 0)
				contribution.share = val / total;
			contributions.add(contribution);
		}
		return new ContributionSet<>(contributions);
	}

	public static <T> ContributionSet<T> calculate(Collection<T> items,
			Function<T> fn) {
		List<ContributionItem<T>> contributions = new ArrayList<>();
		for (T item : items) {
			ContributionItem<T> contribution = new ContributionItem<>();
			contribution.rest = item == null;
			contribution.item = item;
			double val = fn.value(item);
			contribution.amount = val;
			contributions.add(contribution);
		}
		calculateShares(contributions);
		return new ContributionSet<>(contributions);
	}

	/**
	 * Calculates the relative shares of the given contribution items.
	 */
	public static void calculateShares(
			List<? extends ContributionItem<?>> contributions) {
		if (contributions == null || contributions.isEmpty())
			return;
		double refVal = Math.abs(getRefValue(contributions));
		for (ContributionItem<?> c : contributions) {
			if (refVal == 0)
				c.share = (double) 0;
			else
				c.share = c.amount / refVal;
		}
	}

	private static double getRefValue(
			List<? extends ContributionItem<?>> contributions) {
		ContributionItem<?> first = contributions.get(0);
		double max = first.amount;
		double min = max;
		for (int i = 1; i < contributions.size(); i++) {
			ContributionItem<?> next = contributions.get(i);
			double nextVal = next.amount;
			max = Math.max(max, nextVal);
			min = Math.min(min, nextVal);
		}
		return Math.max(Math.abs(max), Math.abs(min));
	}

	public static <T> void sortAscending(List<ContributionItem<T>> items) {
		Collections.sort(items, new Sorter(true));
	}

	public static <T> void sortDescending(List<ContributionItem<T>> items) {
		Collections.sort(items, new Sorter(false));
	}

	/**
	 * Returns the top-contributors of the given list ordered by their
	 * contribution values in descending order. If there are more items than the
	 * given number (maxItems) a rest-item is created at the bottom of the list
	 * which gets the sum of the items not in the list. Thus the returned list
	 * has <code>maxItems</code> entries.
	 */
	public static <T> List<ContributionItem<T>> topWithRest(
			List<ContributionItem<T>> items, int maxItems) {
		if (items == null)
			return Collections.emptyList();
		sortDescending(items);
		if (items.size() <= maxItems)
			return items;
		List<ContributionItem<T>> list = new ArrayList<>();
		ContributionItem<T> restItem = new ContributionItem<>();
		restItem.rest = true;
		for (int i = 0; i < items.size(); i++) {
			ContributionItem<T> item = items.get(i);
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

	@FunctionalInterface
	public interface Function<T> {

		double value(T t);

	}

	private static class Sorter implements Comparator<ContributionItem<?>> {

		private final boolean ascending;

		public Sorter(boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public int compare(ContributionItem<?> o1, ContributionItem<?> o2) {
			if (o1 == null || o2 == null)
				return 0;
			if (o1.rest)
				return 1;
			if (o2.rest)
				return -1;
			if (ascending)
				return Double.compare(o1.amount, o2.amount);
			else
				return -Double.compare(o1.amount, o2.amount);
		}
	}
}
