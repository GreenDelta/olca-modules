package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The simplest kind of result of a calculated product system. This result type
 * is particularly suitable for Monte Carlo simulations or other quick
 * calculations.
 */
public class SimpleResult extends BaseResult {

	/**
	 * The scaling vector $\mathbf{s}$ which is calculated by solving the
	 * equation
	 * <p>
	 * $$\mathbf{A} \ \mathbf{s} = \mathbf{f}$$
	 * <p>
	 * where $\mathbf{A}$ is the technology matrix and $\mathbf{f}$ the final
	 * demand vector of the product system.
	 */
	public double[] scalingVector;

	/**
	 * The total requirements of the products to fulfill the demand of the
	 * product system. As our technology matrix $\mathbf{A}$ is indexed
	 * symmetrically (means rows and columns refer to the same process-product
	 * pair) our product amounts are on the diagonal of the technology matrix
	 * $\mathbf{A}$ and the total requirements can be calculated by the
	 * following equation where $\mathbf{s}$ is the scaling vector ($\odot$
	 * denotes element-wise multiplication):
	 * <p>
	 * $$\mathbf{t} = \text{diag}(\mathbf{A}) \odot \mathbf{s}$$
	 */
	public double[] totalRequirements;

	/**
	 * The inventory result $\mathbf{g}$ of a product system:
	 * <p>
	 * $$\mathbf{g} = \mathbf{B} \ \mathbf{s}$$
	 * <p>
	 * Where $\mathbf{B}$ is the intervention matrix and $\mathbf{s}$ the
	 * scaling vector. Note that inputs have negative values in this vector.
	 */
	public double[] totalFlowResults;

	/**
	 * The LCIA result $\mathbf{h}$ of a product system:
	 * <p>
	 * $$\mathbf{h} = \mathbf{C} \ \mathbf{g}$$
	 * <p>
	 * Where $\mathbf{C}$ is a flow * LCIA category matrix with the
	 * characterization factors and $\mathbf{g}$ the inventory result.
	 */
	public double[] totalImpactResults;

	/**
	 * The total net-costs $k_t$ of the LCC result:
	 * <p>
	 * $$k_t = \mathbf{k} \cdot \mathbf{s}$$
	 * <p>
	 * Where $\mathbf{k}_j$ are the net-costs of process $j$ and $\mathbf{s}_j$
	 * is the scaling factor of that process.
	 */
	public double totalCosts;

	/**
	 * Get the scaling factor $\mathbf{s}_j$ of the given process-product pair
	 * $j$.
	 */
	public double getScalingFactor(ProcessProduct product) {
		int idx = techIndex.getIndex(product);
		if (idx < 0 || idx > scalingVector.length)
			return 0;
		return scalingVector[idx];
	}

	/**
	 * Get the scaling factor $\mathbf{s}_j$ of the given process $j$. When the
	 * process has multiple products in the system it returns the sum of the
	 * scaling factors of all of these process-product pairs.
	 */
	public double getScalingFactor(CategorizedDescriptor process) {
		double factor = 0;
		for (ProcessProduct p : techIndex.getProviders(process)) {
			factor += getScalingFactor(p);
		}
		return factor;
	}

	/**
	 * Get the total inventory result $\mathbf{g}_i$ of the given flow $i$.
	 */
	public double getTotalFlowResult(IndexFlow flow) {
		if (flowIndex == null)
			return 0;
		int idx = flowIndex.of(flow);
		if (idx < 0 || idx >= totalFlowResults.length)
			return 0;
		return adopt(flow, totalFlowResults[idx]);
	}

	/**
	 * Returns the flow results of the inventory result $\mathbf{g}$.
	 */
	public List<FlowResult> getTotalFlowResults() {
		if (flowIndex == null)
			return Collections.emptyList();
		List<FlowResult> results = new ArrayList<>(flowIndex.size());
		flowIndex.each((i, f) -> results.add(
				new FlowResult(f, getTotalFlowResult(f))));
		return results;
	}

	/**
	 * Returns the total LCIA result $\mathbf{h}_i$ of the given LCIA category
	 * $i$.
	 */
	public double getTotalImpactResult(ImpactCategoryDescriptor impact) {
		int idx = impactIndex.of(impact);
		if (idx < 0 || idx >= totalImpactResults.length)
			return 0;
		return totalImpactResults[idx];
	}

	/**
	 * Returns the impact category results for the given result.
	 */
	public List<ImpactResult> getTotalImpactResults() {
		List<ImpactResult> results = new ArrayList<>();
		if (!hasImpactResults())
			return results;
		impactIndex.each((i, d) -> {
			ImpactResult r = new ImpactResult();
			r.impactCategory = d;
			r.value = getTotalImpactResult(d);
			results.add(r);
		});
		return results;
	}

	@Override
	public boolean hasCostResults() {
		return totalCosts != 0;
	}
}
