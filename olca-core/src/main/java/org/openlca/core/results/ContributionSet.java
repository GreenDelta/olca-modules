package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A set of contributions to an overall result.
 */
public class ContributionSet<T> {

	private List<ContributionItem<T>> contributions = new ArrayList<>();

	public ContributionSet(List<ContributionItem<T>> contributions) {
		this.contributions.addAll(contributions);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> ContributionSet<T> empty() {
		ContributionSet set = new ContributionSet(Collections.emptyList());
		return set;
	}

	public List<ContributionItem<T>> getContributions() {
		return contributions;
	}

	public ContributionItem<T> getContribution(T item) {
		if (item == null)
			return null;
		for (ContributionItem<T> contribution : contributions) {
			if (item.equals(contribution.getItem()))
				return contribution;
		}
		return null;
	}

}
