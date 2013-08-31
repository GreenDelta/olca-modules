package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Contributions {

	private Contributions() {
	}

	/**
	 * Calculates a contribution set of the given collection of items. The
	 * contribution values and shares are calculated with the given amount
	 * functions which maps an item to the respective contribution amount of
	 * this item: <br>
	 * <code> 
	 * contributionSet = Contributions.calculate(items, item -> amount)
	 * </code>
	 */
	public static <T> ContributionSet<T> calculate(Collection<T> items,
			Function<T> fn) {
		List<Contribution<T>> contributions = new ArrayList<>();
		for (T item : items) {
			Contribution<T> contribution = new Contribution<>();
			contribution.setItem(item);
			contribution.setAmount(fn.value(item));
			contributions.add(contribution);
		}
		calculateShares(contributions);
		return new ContributionSet<>(contributions);
	}

	private static void calculateShares(
			List<? extends Contribution<?>> contributions) {
		if (contributions == null || contributions.isEmpty())
			return;
		double refVal = getRefValue(contributions);
		for (Contribution<?> c : contributions) {
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
			List<? extends Contribution<?>> contributions) {
		Contribution<?> first = contributions.get(0);
		double max = first.getAmount();
		double min = max;
		for (int i = 1; i < contributions.size(); i++) {
			Contribution<?> next = contributions.get(i);
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
