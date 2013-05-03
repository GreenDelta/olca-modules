package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Indexable;

/**
 * A set of contributions to an overall result. Contains optionally a rest-value
 * which is defined as: </br><code>
 * 
 * rest = result - sum(contributions).
 */
public class ContributionSet<T extends Indexable> {

	private double restValue;
	private List<Contribution<T>> contributions = new ArrayList<>();

	public ContributionSet(List<Contribution<T>> contributions) {
		this.contributions.addAll(contributions);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Indexable> ContributionSet<T> empty() {
		ContributionSet set = new ContributionSet(Collections.emptyList());
		return set;
	}

	public double getRestValue() {
		return restValue;
	}

	public void setRestValue(double restValue) {
		this.restValue = restValue;
	}

	public List<Contribution<T>> getContributions() {
		return contributions;
	}

	public Contribution<T> getContribution(T item) {
		if (item == null)
			return null;
		for (Contribution<T> contribution : contributions) {
			if (item.equals(contribution.getItem()))
				return contribution;
		}
		return null;
	}

}
