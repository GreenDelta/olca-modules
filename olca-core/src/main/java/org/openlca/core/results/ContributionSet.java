package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A set of contributions to an overall result.
 */
public class ContributionSet<T> {

	public final List<ContributionItem<T>> contributions = new ArrayList<>();

	public ContributionSet(List<ContributionItem<T>> contributions) {
		this.contributions.addAll(contributions);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> ContributionSet<T> empty() {
		ContributionSet set = new ContributionSet(Collections.emptyList());
		return set;
	}

	public ContributionItem<T> getContribution(T item) {
		for (ContributionItem<T> contribution : contributions) {
			if (Objects.equals(item, contribution.item))
				return contribution;
		}
		return null;
	}

}
