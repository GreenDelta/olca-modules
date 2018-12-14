package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ContributionResultProvider<T extends ContributionResult> extends
		SimpleResultProvider<T> {

	public ContributionResultProvider(T result, EntityCache cache) {
		super(result, cache);
	}






	public List<FlowResult> getSingleFlowImpacts(
			ImpactCategoryDescriptor impact) {
		List<FlowResult> results = new ArrayList<>();
		result.flowIndex.each(flow -> {
			FlowResult r = getSingleFlowImpact(flow, impact);
			results.add(r);
		});
		return results;
	}

	private FlowResult getSingleFlowImpact(FlowDescriptor flow,
			ImpactCategoryDescriptor impact) {
		FlowIndex index = result.flowIndex;
		double val = result.getSingleFlowImpact(flow.getId(), impact.getId());
		FlowResult r = new FlowResult();
		r.flow = flow;
		r.input = index.isInput(flow.getId());
		r.value = val;
		return r;
	}

	/**
	 * Get the single contributions of the flows to the total result of the
	 * given LCIA category.
	 */
	public ContributionSet<FlowDescriptor> getFlowContributions(
			final ImpactCategoryDescriptor impact) {
		double total = result.getTotalImpactResult(impact.getId());
		return Contributions.calculate(
				result.flowIndex.content(),
				total,
				flow -> result.getSingleFlowImpact(flow.getId(),
						impact.getId()));
	}

	public double getSingleCostResult(ProcessDescriptor process) {
		return result.getSingleCostResult(process.getId());
	}

	public ContributionSet<ProcessDescriptor> getProcessCostContributions() {
		return Contributions.calculate(
				getProcessDescriptors(),
				result.totalCostResult,
				process -> result.getSingleCostResult(process.getId()));
	}

}
