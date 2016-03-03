package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class FullResultProvider extends ContributionResultProvider<FullResult> {

	private final LinkContributions linkContributions;
	private final UpstreamTreeCalculator treeCalculator;

	public FullResultProvider(FullResult result, EntityCache cache) {
		super(result, cache);
		this.linkContributions = result.linkContributions;
		this.treeCalculator = new UpstreamTreeCalculator(result);
	}

	public List<FlowResult> getUpstreamFlowResults(ProcessDescriptor process) {
		FlowIndex index = result.flowIndex;
		List<FlowResult> results = new ArrayList<>();
		for (FlowDescriptor flow : getFlowDescriptors()) {
			double val = result.getUpstreamFlowResult(process.getId(),
					flow.getId());
			if (val == 0)
				continue;
			val = adoptFlowResult(val, flow.getId());
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = index.isInput(flow.getId());
			r.value = val;
			results.add(r);
		}
		return results;
	}

	public FlowResult getUpstreamFlowResult(ProcessDescriptor process,
			FlowDescriptor flow) {
		double val = result
				.getUpstreamFlowResult(process.getId(), flow.getId());
		val = adoptFlowResult(val, flow.getId());
		FlowResult r = new FlowResult();
		r.flow = flow;
		FlowIndex index = result.flowIndex;
		r.input = index.isInput(flow.getId());
		r.value = val;
		return r;
	}

	public List<ImpactResult> getUpstreamImpactResults(ProcessDescriptor process) {
		List<ImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor impact : getImpactDescriptors())
			results.add(getUpstreamImpactResult(process, impact));
		return results;
	}

	public ImpactResult getUpstreamImpactResult(ProcessDescriptor process,
			ImpactCategoryDescriptor impact) {
		double val = result.getUpstreamImpactResult(process.getId(),
				impact.getId());
		ImpactResult r = new ImpactResult();
		r.impactCategory = impact;
		r.value = val;
		return r;
	}

	public double getUpstreamCostResult(ProcessDescriptor process) {
		return result.getUpstreamCostResult(process.getId());
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to
	 * the product input (recipient) of the given link and the calculated
	 * product system. The returned share is a value between 0 and 1.
	 */
	public double getLinkShare(ProcessLink link) {
		return linkContributions.getShare(link);
	}

	public UpstreamTree getTree(FlowDescriptor flow) {
		return treeCalculator.calculate(flow);
	}

	public UpstreamTree getTree(ImpactCategoryDescriptor impact) {
		return treeCalculator.calculate(impact);
	}

	public UpstreamTree getCostTree() {
		return treeCalculator.calculateCosts();
	}

}
