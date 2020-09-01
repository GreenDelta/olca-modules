package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.solutions.SolutionProvider;

/**
 * The `FullResult` extends the `ContributionResult`. It contains additionally
 * the upstream contributions to LCI, LCIA, and LCC results where applicable.
 */
public class FullResult extends ContributionResult {

	public SolutionProvider solutions;

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * inventory result of elementary flow $i$: $\mathbf{U}[i,j]$.
	 */
	public double getUpstreamFlowResult(ProcessProduct product, IndexFlow flow) {
		if (techIndex == null || flowIndex == null)
			return 0;
		int row = flowIndex.of(flow);
		int col = techIndex.getIndex(product);
		double[] m = solutions.intensities(col);
		if (m.length == 0)
			return 0;
		double t = totalRequirements[col] * solutions.getLoopFactor(col);
		return adopt(flow, t * m[row]);
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the inventory
	 * result of elementary flow $i$. When the process has multiple products it is
	 * the sum of the contributions of all of these process-product pairs.
	 */
	public double getUpstreamFlowResult(CategorizedDescriptor process,
			IndexFlow flow) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(process)) {
			total += getUpstreamFlowResult(p, flow);
		}
		return total;
	}

	/**
	 * Get the upstream contributions of the given process $j$ to the inventory
	 * result of all elementary flows in the product system.
	 */
	public List<FlowResult> getUpstreamFlowResults(
			CategorizedDescriptor process) {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each((i, flow) -> {
			double value = getUpstreamFlowResult(process, flow);
			results.add(new FlowResult(flow, value));
		});
		return results;
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * LCIA category result $j$: $\mathbf{V}[i,j]$.
	 */
	public double getUpstreamImpactResult(
			ProcessProduct product,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = techIndex.getIndex(product);
		double[] h = solutions.impacts(col);
		if (h.length == 0)
			return 0;
		double t = totalRequirements[col] * solutions.getLoopFactor(col);
		return t * h[row];
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the LCIA category
	 * result $i$. When the process has multiple products it is the sum of the
	 * contributions of all of these process-product pairs.
	 */
	public double getUpstreamImpactResult(
			CategorizedDescriptor process,
			ImpactCategoryDescriptor impact) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(process)) {
			total += getUpstreamImpactResult(p, impact);
		}
		return total;
	}

	/**
	 * Get the upstream contributions of the given process $j$ to the LCIA category
	 * results.
	 */
	public List<ImpactResult> getUpstreamImpactResults(
			CategorizedDescriptor process) {
		List<ImpactResult> results = new ArrayList<>();
		if (!hasImpactResults())
			return results;
		impactIndex.each((i, impact) -> {
			ImpactResult r = new ImpactResult();
			r.impactCategory = impact;
			r.value = getUpstreamImpactResult(process, impact);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the upstream contribution of the given process-product pair $j$ to the
	 * LCC result: $\mathbf{k}_u[j]$.
	 */
	public double getUpstreamCostResult(ProcessProduct provider) {
		if (!hasCostResults())
			return 0;
		int col = techIndex.getIndex(provider);
		double c = solutions.costs(col);
		double t = totalRequirements[col] * solutions.getLoopFactor(col);
		return c * t;
	}

	/**
	 * Get the upstream contribution of the given process $j$ to the LCC result.
	 * When the process has multiple products it is the sum of the contributions of
	 * all of these process-product pairs.
	 */
	public double getUpstreamCostResult(CategorizedDescriptor process) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(process)) {
			total += getUpstreamCostResult(p);
		}
		return total;
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to the
	 * product input (recipient) of the given link and the calculated product
	 * system. The returned share is a value between 0 and 1.
	 */
	public double getLinkShare(ProcessLink link) {
		ProcessProduct provider = techIndex.getProvider(link.providerId,
				link.flowId);
		int providerIdx = techIndex.getIndex(provider);
		if (providerIdx < 0)
			return 0;
		double amount = 0.0;
		for (ProcessProduct process : techIndex.getProviders(link.processId)) {
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

	/**
	 * Calculate the upstream tree for the given flow.
	 */
	public UpstreamTree getTree(IndexFlow flow) {
		int i = flowIndex.of(flow);
		double[] u = upstreamFlowResults.getRow(i);
		return new UpstreamTree(flow, this, u);
	}

	/**
	 * Calculate the upstream tree for the given LCIA category.
	 */
	public UpstreamTree getTree(ImpactCategoryDescriptor impact) {
		int i = impactIndex.of(impact.id);
		double[] u = upstreamImpactResults.getRow(i);
		return new UpstreamTree(impact, this, u);
	}

	/**
	 * Calculate the upstream tree for the LCC result as costs.
	 */
	public UpstreamTree getCostTree() {
		return new UpstreamTree(this, upstreamCostResults.getRow(0));
	}

	/**
	 * Calculate the upstream tree for the LCC result as added value.
	 */
	public UpstreamTree getAddedValueTree() {
		double[] u = upstreamCostResults.getRow(0);
		for (int i = 0; i < u.length; i++) {
			u[i] = -u[i];
		}
		return new UpstreamTree(this, u);
	}

}
