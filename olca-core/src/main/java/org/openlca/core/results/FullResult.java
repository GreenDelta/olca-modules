package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The `FullResult` extends the `ContributionResult`. It contains additionally
 * the upstream contributions to LCI, LCIA, and LCC results where applicable.
 */
public class FullResult extends ContributionResult {

	/**
	 * The *scaled* technology matrix $\mathbf{A*}$ of the product system. In
	 * this matrix each column $j$ is scaled by the respective scaling factor
	 * $\mathbf{s}_j$:
	 *
	 * $$\mathbf{A*} = \mathbf{A} \ \text{diag}(\mathbf{s})$$
	 */
	public IMatrix techMatrix;

	/**
	 * The loop factor $c_r$ indicates whether the reference process of the
	 * product system is part of a product loop ($c_r \neq 1$ when this is the
	 * case). It can be calculated via:
	 * 
	 * $$c_r = \frac{\mathbf{s}[r] \mathbf{A}[r,r]}{\mathbf{f}[r]}$$
	 * 
	 * Where $r$ is the index of the reference product of the system. Some
	 * upstream results have to be corrected by $c_r$ when $c_r \neq 1$ to avoid
	 * double counting of these loop contributions.
	 */
	public double loopFactor;

	//@formatter:off
	/**
	 * An elementary flow * process-product matrix that contains the upstream
	 * contributions (including the direct contributions) of the processes to
	 * the inventory result. It can be calculated by column-wise scaling of the
	 * result of the matrix-matrix multiplication of the intervention matrix
	 * $\mathbf{B}$ with the inverse of the technology matrix $\mathbf{A}$ by
	 * the total requirements $\mathbf{t}$:
	 * 
	 * $$\mathbf{U} = (\mathbf{B} \ \mathbf{A}^{-1}) \ \text{diag}(c_r \ \mathbf{t})$$
	 * 
	 * When the reference process itself is located in a loop the total requirements
	 * need to be multiplied with the loop factor $c_r$ to avoid double counting
	 * of the loop contributions (as they are contained in $\mathbf{A}^{-1}$ and
	 * $\mathbf{t}$).
	 */
	public IMatrix upstreamFlowResults;
	//@formatter:on

	//@formatter:off
	/**
	 * An elementary flow * process-product matrix that contains the direct
	 * contributions of the processes to the inventory result. This can be
	 * calculated by column-wise scaling of the intervention matrix $\mathbf{B}$
	 * with the scaling vector $\mathbf{s}$:
	 *
	 * $$\mathbf{G} = \mathbf{B} \ \text{diag}(\mathbf{s})$$
	 */	
	public IMatrix upstreamImpactResults;
	//@formatter:on

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
			ProcessProduct provider,
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
		for (ProcessProduct p : techIndex.getProviders(process)) {
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
			ProcessProduct provider,
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
		for (ProcessProduct p : techIndex.getProviders(process)) {
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
	public double getUpstreamCostResult(ProcessProduct provider) {
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
		for (ProcessProduct p : techIndex.getProviders(process)) {
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
