package org.openlca.core.results;

import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * Calculates the contributions of single process results grouped by a given
 * grouping set.
 */
public class GroupingContribution {

	private final LcaResult result;
	private final List<ProcessGrouping> groupings;

	public GroupingContribution(LcaResult result,
			List<ProcessGrouping> groupings) {
		this.result = result;
		this.groupings = groupings;
	}

	/**
	 * Calculates contributions to an inventory flow.
	 */
	public List<Contribution<ProcessGrouping>> calculate(EnviFlow flow) {
		if (result == null || groupings == null)
			return Collections.emptyList();
		double total = result.totalFlowOf(flow);
		var techIdx = result.techIndex();
		return Contributions.calculate(groupings, total, grouping -> {
			double amount = 0;
			for (var p : grouping.processes) {
				for (var techFlow : techIdx.getProviders(p)) {
					amount += result.directFlowOf(flow, techFlow);
				}
			}
			return amount;
		});
	}

	/**
	 * Calculates contributions to an impact assessment method.
	 */
	public List<Contribution<ProcessGrouping>> calculate(ImpactDescriptor impact) {
		if (result == null || groupings == null)
			return Collections.emptyList();
		double total = result.totalImpactOf(impact);
		var techIdx = result.techIndex();
		return Contributions.calculate(groupings, total, grouping -> {
			double amount = 0;
			for (var p : grouping.processes) {
				for (var techFlow : techIdx.getProviders(p)) {
					amount += result.directImpactOf(impact, techFlow);
				}
			}
			return amount;
		});
	}
}
