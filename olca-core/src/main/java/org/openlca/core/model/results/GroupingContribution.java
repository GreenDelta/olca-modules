package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.AnalysisResult;

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
	public ContributionSet<ProcessGrouping> calculate(Flow flow) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		List<Contribution<ProcessGrouping>> contributions = new ArrayList<>();
		for (ProcessGrouping grouping : groupings) {
			Contribution<ProcessGrouping> contribution = new Contribution<>();
			contribution.setItem(grouping);
			double amount = 0;
			for (Process p : grouping.getProcesses())
				amount += result.getSingleResult(p, flow);
			contribution.setAmount(amount);
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}

	/** Calculates contributions to an impact assessment method. */
	public ContributionSet<ProcessGrouping> calculate(
			ImpactCategoryDescriptor impact) {
		if (result == null || groupings == null)
			return ContributionSet.empty();
		List<Contribution<ProcessGrouping>> contributions = new ArrayList<>();
		for (ProcessGrouping grouping : groupings) {
			Contribution<ProcessGrouping> contribution = new Contribution<>();
			contribution.setItem(grouping);
			double amount = 0;
			for (Process p : grouping.getProcesses())
				amount += result.getSingleResult(p, impact);
			contribution.setAmount(amount);
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}
}
