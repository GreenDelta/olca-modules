package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collection;
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
	 * share = item -> amount / totalAmount
	 * </code> An contribution item is set as the "rest" item
	 * (contributionItem.isRest = true) if the item in the collection is null).
	 */
	public static <T> ContributionSet<T> calculate(Collection<T> items,
			double totalAmount, Function<T> fn) {
		List<ContributionItem<T>> contributions = new ArrayList<>();
		for (T item : items) {
			ContributionItem<T> contribution = new ContributionItem<>();
			contribution.setRest(item == null);
			contribution.setItem(item);
			double val = fn.value(item);
			contribution.setAmount(val);
			if (totalAmount != 0)
				contribution.setShare(val / totalAmount);
			contributions.add(contribution);
		}
		return new ContributionSet<>(contributions);
	}

	/**
	 * Calculates the relative shares of the given contribution items.
	 */
	public static void calculateShares(
			List<? extends ContributionItem<?>> contributions) {
		if (contributions == null || contributions.isEmpty())
			return;
		double refVal = getRefValue(contributions);
		for (ContributionItem<?> c : contributions) {
			double share = share(c.getAmount(), refVal);
			c.setShare(share);
		}
	}

	private static double share(double val, double refValue) {
		if (refValue == 0)
			return 0;
		return (val / refValue);
	}

	private static double getRefValue(
			List<? extends ContributionItem<?>> contributions) {
		ContributionItem<?> first = contributions.get(0);
		double max = first.getAmount();
		double min = max;
		for (int i = 1; i < contributions.size(); i++) {
			ContributionItem<?> next = contributions.get(i);
			double nextVal = next.getAmount();
			max = Math.max(max, nextVal);
			min = Math.min(min, nextVal);
		}
		return Math.max(Math.abs(max), Math.abs(min));
	}

	public interface Function<T> {

		double value(T t);

	}
}
