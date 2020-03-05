package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A set of contributions to an overall result.
 * 
 * @deprecated This class just wraps a list of contributions and thus is not
 *             very useful.
 */
@Deprecated
public class ContributionSet<T> {

	public final List<Contribution<T>> contributions = new ArrayList<>();

	public ContributionSet(List<Contribution<T>> contributions) {
		this.contributions.addAll(contributions);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> ContributionSet<T> empty() {
		ContributionSet set = new ContributionSet(Collections.emptyList());
		return set;
	}

	public Contribution<T> getContribution(T item) {
		for (Contribution<T> contribution : contributions) {
			if (Objects.equals(item, contribution.item))
				return contribution;
		}
		return null;
	}

}
