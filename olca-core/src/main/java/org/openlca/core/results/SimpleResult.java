package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
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
	 * 
	 * $$\mathbf{A} \ \mathbf{s} = \mathbf{f}$$
	 * 
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
	 * 
	 * $$\mathbf{t} = \text{diag}(\mathbf{A}) \odot \mathbf{s}$$
	 */
	public double[] totalRequirements;

	/**
	 * The inventory result $\mathbf{g}$ of a product system:
	 * 
	 * $$\mathbf{g} = \mathbf{B} \ \mathbf{s}$$
	 * 
	 * Where $\mathbf{B}$ is the intervention matrix and $\mathbf{s}$ the
	 * scaling vector. Note that inputs have negative values in this vector.
	 */
	public double[] totalFlowResults;

	/**
	 * The LCIA result $\mathbf{h}$ of a product system:
	 * 
	 * $$\mathbf{h} = \mathbf{C} \ \mathbf{g}$$
	 * 
	 * Where $\mathbf{C}$ is a flow * LCIA category matrix with the
	 * characterization factors and $\mathbf{g}$ the inventory result.
	 */
	public double[] totalImpactResults;

	/**
	 * The total net-costs $k_t$ of the LCC result:
	 * 
	 * $$k_t = \mathbf{k} \cdot \mathbf{s}$$
	 * 
	 * Where $\mathbf{k}_j$ are the net-costs of process $j$ and $\mathbf{s}_j$
	 * is the scaling factor of that process.
	 */
	public double totalCosts;

	/**
	 * Get the scaling factor of the given process-product.
	 *
	 * TODO: doc
	 */
	public double getScalingFactor(ProcessProduct provider) {
		int idx = techIndex.getIndex(provider);
		if (idx < 0 || idx > scalingVector.length)
			return 0;
		return scalingVector[idx];
	}

	/**
	 * Get the sum of all scaling factors for the products of the process or
	 * product system with the given ID.
	 */
	public double getScalingFactor(CategorizedDescriptor d) {
		double factor = 0;
		for (ProcessProduct p : techIndex.getProviders(d)) {
			factor += getScalingFactor(p);
		}
		return factor;
	}

	public double getTotalFlowResult(FlowDescriptor flow) {
		int idx = flowIndex.of(flow);
		if (idx < 0 || idx >= totalFlowResults.length)
			return 0;
		return adopt(flow, totalFlowResults[idx]);
	}

	/**
	 * Returns the flow results of the inventory results.
	 */
	public List<FlowResult> getTotalFlowResults() {
		List<FlowResult> results = new ArrayList<>();
		flowIndex.each(d -> {
			FlowResult r = new FlowResult();
			r.flow = d;
			r.input = flowIndex.isInput(d);
			r.value = getTotalFlowResult(d);
			results.add(r);
		});
		return results;
	}

	/**
	 * Returns the total result of the LCIA category with the given ID.
	 */
	public double getTotalImpactResult(ImpactCategoryDescriptor impact) {
		int idx = impactIndex.of(impact);
		if (idx < 0 || idx >= totalImpactResults.length)
			return 0;
		return totalImpactResults[idx];
	}

	/**
	 * Returns the impact category results for the given result. In contrast to
	 * the flow results, entries are also generated for 0-values.
	 */
	public List<ImpactResult> getTotalImpactResults() {
		List<ImpactResult> results = new ArrayList<>();
		if (!hasImpactResults())
			return results;
		impactIndex.each(d -> {
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
