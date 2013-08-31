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

	private AnalysisResult result;
	private List<ProcessGrouping> groupings;

	public GroupingContribution(AnalysisResult result,
			List<ProcessGrouping> groupings) {
		this.result = result;
		this.groupings = groupings;
	}

	/** Calculates contributions to an inventory flow. */
	public ContributionSet<ProcessGrouping> calculate(final FlowDescriptor flow) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		return Contributions.calculate(groupings,
				new Function<ProcessGrouping>() {
					@Override
					public double value(ProcessGrouping grouping) {
						double amount = 0;
						for (ProcessDescriptor p : grouping.getProcesses())
							amount += result.getSingleFlowResult(p.getId(),
									flow.getId());
						return amount;
					}
				});
	}

	/** Calculates contributions to an impact assessment method. */
	public ContributionSet<ProcessGrouping> calculate(
			final ImpactCategoryDescriptor impact) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		return Contributions.calculate(groupings,
				new Function<ProcessGrouping>() {
					@Override
					public double value(ProcessGrouping grouping) {
						double amount = 0;
						for (ProcessDescriptor p : grouping.getProcesses())
							amount += result.getSingleImpactResult(p.getId(),
									impact.getId());
						return amount;
					}
				});
	}
}
