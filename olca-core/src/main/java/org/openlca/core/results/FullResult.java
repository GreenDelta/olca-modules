package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * A full result extends the base and contribution result by providing
 * additionally all calculated upstream-results for intervention flows and LCIA
 * categories for each single process-product in the system.
 */
public class FullResult extends ContributionResult {

	/**
	 * The *scaled* technology matrix of the product system: A * diagm(0 => s).
	 */
	public IMatrix techMatrix;

	/**
	 * TODO: add doc ...
	 */
	public double loopFactor;

	/**
	 * The upstream flow results in a matrix where the flows are mapped to the
	 * rows and the process-products to the columns. Inputs have negative values
	 * here.
	 */
	public IMatrix upstreamFlowResults;

	/**
	 * The upstream LCIA category results in a matrix where the LCIA categories
	 * are mapped to the rows and the process-products to the columns.
	 */
	public IMatrix upstreamImpactResults;

	/**
	 * The upstream cost results is a simple row vector where each entry
	 * contains the upstream costs for the product at the given index.
	 */
	public IMatrix upstreamCostResults;

	/**
	 * Get the upstream flow result of the flow with the given ID for the given
	 * process-product. Inputs have negative values here.
	 */
	public double getUpstreamFlowResult(
			Provider provider,
			FlowDescriptor flow) {
		int row = flowIndex.of(flow);
		int col = techIndex.getIndex(provider);
		return adopt(flow, getValue(upstreamFlowResults, row, col));
	}

	/**
	 * Get the sum of the upstream flow results of the flow with the given ID
	 * for all products of the process with the given ID. Inputs have negative
	 * values here.
	 */
	public double getUpstreamFlowResult(
			CategorizedDescriptor process,
			FlowDescriptor flow) {
		double total = 0;
		for (Provider p : techIndex.getProviders(process)) {
			total += getUpstreamFlowResult(p, flow);
		}
		return total;
	}

	public List<FlowResult> getUpstreamFlowResults(
			CategorizedDescriptor process) {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each(flow -> {
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = flowIndex.isInput(flow);
			r.value = getUpstreamFlowResult(process, flow);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the upstream LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 */
	public double getUpstreamImpactResult(
			Provider provider,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = techIndex.getIndex(provider);
		return getValue(upstreamImpactResults, row, col);
	}

	/**
	 * Get the sum of the upstream LCIA category results of the LCIA category
	 * with the given ID for all products of the process with the given ID.
	 */
	public double getUpstreamImpactResult(
			CategorizedDescriptor process,
			ImpactCategoryDescriptor impact) {
		double total = 0;
		for (Provider p : techIndex.getProviders(process)) {
			total += getUpstreamImpactResult(p, impact);
		}
		return total;
	}

	public List<ImpactResult> getUpstreamImpactResults(
			CategorizedDescriptor process) {
		List<ImpactResult> results = new ArrayList<>();
		if (!hasImpactResults())
			return results;
		impactIndex.each(impact -> {
			ImpactResult r = new ImpactResult();
			r.impactCategory = impact;
			r.value = getUpstreamImpactResult(process, impact);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the upstream cost result of the given process-product.
	 */
	public double getUpstreamCostResult(Provider provider) {
		if (!hasCostResults())
			return 0;
		int col = techIndex.getIndex(provider);
		return getValue(upstreamCostResults, 0, col);
	}

	/**
	 * Get the upstream cost result of the given process.
	 */
	public double getUpstreamCostResult(CategorizedDescriptor process) {
		double total = 0;
		for (Provider p : techIndex.getProviders(process)) {
			total += getUpstreamCostResult(p);
		}
		return total;
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to
	 * the product input (recipient) of the given link and the calculated
	 * product system. The returned share is a value between 0 and 1.
	 */
	public double getLinkShare(ProcessLink link) {
		Provider provider = techIndex.getProvider(link.providerId, link.flowId);
		int providerIdx = techIndex.getIndex(provider);
		if (providerIdx < 0)
			return 0;
		double amount = 0.0;
		for (Provider process : techIndex.getProviders(link.processId)) {
			int processIdx = techIndex.getIndex(process);
			amount += techMatrix.get(providerIdx, processIdx);
		}
		if (amount == 0)
			return 0;
		double total = techMatrix.get(providerIdx, providerIdx);
		if (total == 0)
			return 0;
		return -amount / total;
	}

	public UpstreamTree getTree(FlowDescriptor flow) {
		int i = flowIndex.of(flow);
		double[] u = upstreamFlowResults.getRow(i);
		return new UpstreamTree(flow, this, u);
	}

	public UpstreamTree getTree(ImpactCategoryDescriptor impact) {
		int i = impactIndex.of(impact.getId());
		double[] u = upstreamImpactResults.getRow(i);
		return new UpstreamTree(impact, this, u);
	}

	public UpstreamTree getCostTree() {
		return new UpstreamTree(this, upstreamCostResults.getRow(0));
	}

	public UpstreamTree getAddedValueTree() {
		double[] u = upstreamCostResults.getRow(0);
		for (int i = 0; i < u.length; i++) {
			u[i] = -u[i];
		}
		return new UpstreamTree(this, u);
	}

}
