package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The `ContributionResult` extends the `SimpleResult` type. It also contains
 * all direct contributions of the processes to the LCI and LCIA results.
 * Additionally, it contains the contributions of the (elementary) flows to the
 * LCIA results.
 */
public class ContributionResult extends SimpleResult {

	/**
	 * An elementary flow * process-product matrix that contains the direct
	 * contributions of the processes to the inventory result. This can be
	 * calculated by column-wise scaling of the intervention matrix $\mathbf{B}$
	 * with the scaling vector $\mathbf{s}$:
	 *
	 * $$\mathbf{G} = \mathbf{B} \ \text{diag}(\mathbf{s})$$
	 */
	public IMatrix directFlowResults;

	/**
	 * A LCIA category * process-product matrix that contains the direct
	 * contributions of the processes to the LCIA result. This can be calculated
	 * by a matrix-matrix multiplication of the direct inventory contributions
	 * $\mathbf{G}$ with the matrix with the characterization factors
	 * $\mathbf{C}$:
	 *
	 * $$\mathbf{H} = \mathbf{C} \ \mathbf{G}$$
	 */
	public IMatrix directImpactResults;

	/**
	 * Contains the direct contributions $\mathbf{k}_s$ of the process-product
	 * pairs to the total net-costs ($\odot$ denotes element-wise
	 * multiplication):
	 *
	 * $$\mathbf{k}_s = \mathbf{k} \odot \mathbf{s}$$
	 */
	public double[] directCostResults;

	/**
	 * A LCIA category * flow matrix that contains the direct contributions of
	 * the elementary flows to the LCIA result. This matrix can be calculated by
	 * column-wise scaling of the matrix with the characterization factors
	 * $\mathbf{C}$ with the inventory result $\mathbf{g}$:
	 *
	 * $$\mathbf{H} = \mathbf{C} \ \text{diag}(\mathbf{g})$$
	 */
	public IMatrix directFlowImpacts;

	/**
	 * A LCIA category * flow matrix $\mathbf{C}$ the contains the
	 * characterization factors.
	 */
	public IMatrix impactFactors;

	@Override
	public boolean hasCostResults() {
		return directCostResults != null;
	}

	/**
	 * Get the direct contribution of the given process-product pair $j$ to the
	 * inventory result of elementary flow $i$: $\mathbf{G}[i,j]$.
	 */
	public double getDirectFlowResult(ProcessProduct product,
			FlowDescriptor flow) {
		if (!hasFlowResults())
			return 0;
		int row = flowIndex.of(flow);
		int col = techIndex.getIndex(product);
		return adopt(flow, getValue(directFlowResults, row, col));
	}

	/**
	 * Get the direct contribution of the given process $j$ to the inventory
	 * result of elementary flow $i$. When the process has multiple products it
	 * is the sum of the contributions of all of these process-product pairs.
	 */
	public double getDirectFlowResult(CategorizedDescriptor process,
			FlowDescriptor flow) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(process)) {
			total += getDirectFlowResult(p, flow);
		}
		return total;
	}

	/**
	 * Get the direct contributions of the given process $j$ to the inventory
	 * result of all elementary flows in the product system.
	 */
	public List<FlowResult> getFlowContributions(
			CategorizedDescriptor process) {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each(flow -> {
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = flowIndex.isInput(flow.getId());
			r.value = getDirectFlowResult(process, flow);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the direct contributions of the processes in the system to the
	 * inventory result of the given flow.
	 */
	public ContributionSet<CategorizedDescriptor> getProcessContributions(
			FlowDescriptor flow) {
		return Contributions.calculate(
				getProcesses(),
				getTotalFlowResult(flow),
				d -> getDirectFlowResult(d, flow));
	}

	/**
	 * Get the direct contribution of the given process-product pair $j$ to the
	 * LCIA category result $j$: $\mathbf{D}[i,j]$.
	 */
	public double getDirectImpactResult(ProcessProduct product,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = techIndex.getIndex(product);
		return getValue(directImpactResults, row, col);
	}

	/**
	 * Get the direct contribution of the given process $j$ to the LCIA category
	 * result $i$. When the process has multiple products it is the sum of the
	 * contributions of all of these process-product pairs.
	 */
	public double getDirectImpactResult(CategorizedDescriptor process,
			ImpactCategoryDescriptor impact) {
		double total = 0;
		for (ProcessProduct p : techIndex.getProviders(process)) {
			total += getDirectImpactResult(p, impact);
		}
		return total;
	}

	/**
	 * Get the direct contributions of the given process $j$ to the LCIA
	 * category results.
	 */
	public List<ImpactResult> getImpactContributions(
			CategoryDescriptor process) {
		List<ImpactResult> results = new ArrayList<>();
		impactIndex.each(impact -> {
			ImpactResult r = new ImpactResult();
			r.impactCategory = impact;
			r.value = getDirectImpactResult(process, impact);
			results.add(r);
		});
		return results;
	}

	/**
	 * Get the direct contributions of the processes in the system to the LCIA
	 * result of the given LCIA category.
	 */
	public ContributionSet<CategorizedDescriptor> getProcessContributions(
			ImpactCategoryDescriptor impact) {
		return Contributions.calculate(
				getProcesses(),
				getTotalImpactResult(impact),
				d -> getDirectImpactResult(d, impact));
	}

	/**
	 * Get the direct contribution of the given process-product pair $j$ to the
	 * LCC result: $\mathbf{k}_s[j]$.
	 */
	public double getDirectCostResult(ProcessProduct product) {
		if (!hasCostResults())
			return 0;
		int col = techIndex.getIndex(product);
		if (col >= directCostResults.length)
			return 0;
		return directCostResults[col];
	}

	/**
	 * Get the direct contribution of the given process $j$ to the LCC result.
	 * When the process has multiple products it is the sum of the contributions
	 * of all of these process-product pairs.
	 */
	public double getDirectCostResult(CategorizedDescriptor process) {
		double total = 0;
		for (ProcessProduct provider : techIndex.getProviders(process)) {
			total += getDirectCostResult(provider);
		}
		return total;
	}

	/**
	 * Get the direct contributions of all processes to the LCC result.
	 */
	public ContributionSet<CategorizedDescriptor> getProcessCostContributions() {
		return Contributions.calculate(
				getProcesses(),
				totalCosts,
				d -> getDirectCostResult(d));
	}

	/**
	 * Get the direct contribution of the given elementary flow to the LCIA
	 * result of the given LCIA category.
	 */
	public double getDirectFlowImpact(FlowDescriptor flow,
			ImpactCategoryDescriptor impact) {
		if (!hasImpactResults())
			return 0;
		int row = impactIndex.of(impact);
		int col = flowIndex.of(flow);
		return getValue(directFlowImpacts, row, col);
	}

	/**
	 * Get the contributions of all elementary flows to the given LCA category.
	 */
	public List<FlowResult> getFlowContributions(
			ImpactCategoryDescriptor impact) {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each(flow -> {
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = flowIndex.isInput(flow);
			r.value = getDirectFlowImpact(flow, impact);
			results.add(r);
		});
		return results;
	}

	double getValue(IMatrix matrix, int row, int col) {
		if (matrix == null)
			return 0d;
		if (row < 0 || row >= matrix.rows())
			return 0d;
		if (col < 0 || col >= matrix.columns())
			return 0d;
		return matrix.get(row, col);
	}

}
