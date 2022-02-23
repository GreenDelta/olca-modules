package org.openlca.core.results;

import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

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
	public List<Contribution<ProcessGrouping>> calculate(
			EnviFlow flow) {
		if (result == null || groupings == null)
			return Collections.emptyList();
		double total = result.getTotalFlowResult(flow);
		return Contributions.calculate(groupings, total, grouping -> {
			double amount = 0;
			for (RootDescriptor p : grouping.processes) {
				amount += result.getDirectFlowResult(p, flow);
			}
			return amount;
		});
	}

	/** Calculates contributions to an impact assessment method. */
	public List<Contribution<ProcessGrouping>> calculate(
			final ImpactDescriptor impact) {
		if (result == null || groupings == null)
			return Collections.emptyList();
		double total = result.getTotalImpactResult(impact);
		return Contributions.calculate(groupings, total, grouping -> {
			double amount = 0;
			for (RootDescriptor p : grouping.processes) {
				amount += result.getDirectImpactResult(p, impact);
			}
			return amount;
		});
	}
}
