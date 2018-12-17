package org.openlca.core.results;

import java.util.List;

import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * Calculates the contributions of single process results grouped by a given
 * grouping set.
 */
public class GroupingContribution {

	private ContributionResult result;
	private List<ProcessGrouping> groupings;

	public GroupingContribution(ContributionResult result,
			List<ProcessGrouping> groupings) {
		this.result = result;
		this.groupings = groupings;
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<ProcessGrouping> calculate(
			final FlowDescriptor flow) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		double total = result.getTotalFlowResult(flow);
		return Contributions.calculate(groupings, total, grouping -> {
			double amount = 0;
			for (CategorizedDescriptor p : grouping.processes) {
				amount += result.getDirectFlowResult(p, flow);
			}
			return amount;
		});
	}

	/** Calculates contributions to an impact assessment method. */
	public ContributionSet<ProcessGrouping> calculate(
			final ImpactCategoryDescriptor impact) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		double total = result.getTotalImpactResult(impact);
		return Contributions.calculate(groupings, total, grouping -> {
			double amount = 0;
			for (CategorizedDescriptor p : grouping.processes) {
				amount += result.getDirectImpactResult(p, impact);
			}
			return amount;
		});
	}
}
