package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.Cache;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * Calculates the contributions of the elementary flows to the impact category
 * results of an analysis result.
 */
public class FlowImpactContribution {

	private AnalysisResult result;
	private long refProcess;
	private Cache cache;

	public FlowImpactContribution(AnalysisResult result, Cache cache) {
		this.result = result;
		this.cache = cache;
		refProcess = result.getProductIndex().getRefProduct().getFirst();
	}

	/** Calculate the flow contributions for the given impact. */
	public ContributionSet<FlowDescriptor> calculate(
			ImpactCategoryDescriptor impact) {
		List<Contribution<FlowDescriptor>> contributions = new ArrayList<>();
		for (long flowId : result.getFlowIndex().getFlowIds()) {
			double lciVal = result.getTotalFlowResult(refProcess, flowId);
			double factor = result.getImpactFactor(impact.getId(), flowId);
			Contribution<FlowDescriptor> contribution = new Contribution<>();
			contribution.setAmount(factor * lciVal);
			contribution.setItem(cache.getFlowDescriptor(flowId));
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}
}
