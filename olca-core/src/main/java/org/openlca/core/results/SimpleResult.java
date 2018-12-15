package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.Provider;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The simplest kind of result of a calculated product system. It contains the
 * total interventions and optionally the total impact assessment results of the
 * product system. This result type is suitable for Monte-Carlo-Simulations or
 * other quick calculations.
 */
public class SimpleResult extends BaseResult {

	public double[] scalingFactors;

	/**
	 * This is a vector which contains for each process product the total amount
	 * of this product to fulfill the demand of a product system. The amount is
	 * given in the reference unit of the respective flow and can be calculated
	 * for a process product i via:
	 *
	 * tr_i = s_i * A_{i,i}
	 *
	 * where s_i is the scaling factor for the process product and A{i, i} the
	 * respective entry in the technology matrix.
	 */
	public double[] totalRequirements;

	/**
	 * The total results of all intervention flows. Note that inputs have a
	 * negative value.
	 */
	public double[] totalFlowResults;

	/**
	 * The total results of all LCIA categories.
	 */
	public double[] totalImpactResults;

	/**
	 * Sum of the net-costs for all products in a product system.
	 */
	public double totalCosts;

	/**
	 * Get the scaling factor of the given process-product.
	 *
	 * TODO: doc
	 */
	public double getScalingFactor(Provider provider) {
		int idx = techIndex.getIndex(provider);
		if (idx < 0 || idx > scalingFactors.length)
			return 0;
		return scalingFactors[idx];
	}

	/**
	 * Get the sum of all scaling factors for the products of the process or
	 * product system with the given ID.
	 */
	public double getScalingFactor(CategorizedDescriptor d) {
		double factor = 0;
		for (Provider p : techIndex.getProviders(d)) {
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
