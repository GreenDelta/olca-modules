package org.openlca.core.results;

import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.Contributions.Function;

/**
 * Calculates the contributions of the elementary flows to the impact category
 * results of an analysis result.
 */
public class FlowImpactContribution {

	private AnalysisResult result;
	private long refProcess;
	private EntityCache cache;

	public FlowImpactContribution(AnalysisResult result, EntityCache cache) {
		this.result = result;
		this.cache = cache;
		refProcess = result.getProductIndex().getRefProduct().getFirst();
	}

	/** Calculate the flow contributions for the given impact. */
	public ContributionSet<FlowDescriptor> calculate(
			final ImpactCategoryDescriptor impact) {
		Set<FlowDescriptor> flows = result.getFlowResults().getFlows(cache);
		return Contributions.calculate(flows, new Function<FlowDescriptor>() {
			@Override
			public double value(FlowDescriptor flow) {
				long flowId = flow.getId();
				double lciVal = result.getTotalFlowResult(refProcess, flowId);
				double factor = result.getImpactFactor(impact.getId(), flowId);
				return factor * lciVal;
			}
		});
	}
}
