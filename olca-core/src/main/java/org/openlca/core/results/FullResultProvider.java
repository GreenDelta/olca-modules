package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class FullResultProvider extends ContributionResultProvider<FullResult> {

	public FullResultProvider(FullResult result, EntityCache cache) {
		super(result, cache);
	}

	public List<FlowResult> getUpstreamFlowResults(ProcessDescriptor process) {
		FlowIndex index = result.flowIndex;
		List<FlowResult> results = new ArrayList<>();
		index.each(flow -> {
			double val = result.getUpstreamFlowResult(process.getId(),
					flow.getId());
			if (val == 0)
				return;
			val = adoptFlowResult(val, flow.getId());
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = index.isInput(flow.getId());
			r.value = val;
			results.add(r);
		});
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

	public List<ImpactResult> getUpstreamImpactResults(
			ProcessDescriptor process) {
		List<ImpactResult> results = new ArrayList<>();
		if (!hasImpactResults())
			return results;
		result.impactIndex.each(impact -> results
				.add(getUpstreamImpactResult(process, impact)));
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
		TechIndex idx = result.techIndex;
		Provider provider = idx.getProvider(link.providerId, link.flowId);
		int providerIdx = idx.getIndex(provider);
		if (providerIdx < 0)
			return 0;
		double amount = 0.0;
		for (Provider process : idx.getProviders(link.processId)) {
			int processIdx = idx.getIndex(process);
			amount += result.techMatrix.get(providerIdx, processIdx);
		}
		if (amount == 0)
			return 0;
		double total = result.techMatrix.get(providerIdx, providerIdx);
		if (total == 0)
			return 0;
		return -amount / total;
	}

	public UpstreamTree getTree(FlowDescriptor flow) {
		int i = result.flowIndex.of(flow);
		double[] u = result.upstreamFlowResults.getRow(i);
		return new UpstreamTree(flow, result, u);
	}

	public UpstreamTree getTree(ImpactCategoryDescriptor impact) {
		int i = result.impactIndex.of(impact.getId());
		double[] u = result.upstreamImpactResults.getRow(i);
		return new UpstreamTree(impact, result, u);
	}

	public UpstreamTree getCostTree() {
		return new UpstreamTree(result, result.upstreamCostResults.getRow(0));
	}

	public UpstreamTree getAddedValueTree() {
		double[] u = result.upstreamCostResults.getRow(0);
		for (int i = 0; i < u.length; i++) {
			u[i] = -u[i];
		}
		return new UpstreamTree(result, u);
	}

}
