package org.openlca.core.results;

import java.util.List;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.Contributions.Function;

/**
 * Calculates the contributions of single process results grouped by a given
 * grouping set.
 */
public class GroupingContribution {

	private ContributionResultProvider<?> result;
	private List<ProcessGrouping> groupings;

	public GroupingContribution(ContributionResultProvider<?> result,
			List<ProcessGrouping> groupings) {
		this.result = result;
		this.groupings = groupings;
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<ProcessGrouping> calculate(final FlowDescriptor flow) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		double total = result.getTotalFlowResult(flow).value;
		return Contributions.calculate(groupings, total,
				new Function<ProcessGrouping>() {
					@Override
					public double value(ProcessGrouping grouping) {
						double amount = 0;
						for (ProcessDescriptor p : grouping.processes)
							amount += result.getSingleFlowResult(p, flow).value;
						return amount;
					}
				});
	}

	/** Calculates contributions to an impact assessment method. */
	public ContributionSet<ProcessGrouping> calculate(
			final ImpactCategoryDescriptor impact) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		double total = result.getTotalImpactResult(impact).value;
		return Contributions.calculate(groupings, total,
				new Function<ProcessGrouping>() {
					@Override
					public double value(ProcessGrouping grouping) {
						double amount = 0;
						for (ProcessDescriptor p : grouping.processes)
							amount += result.getSingleImpactResult(p, impact).value;
						return amount;
					}
				});
	}
}
