package org.openlca.core.results;

import java.util.List;

import org.openlca.core.matrix.LongPair;

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
	public double totalCostResult;

	/**
	 * Get the scaling factor of the given process-product.
	 */
	public double getScalingFactor(LongPair processProduct) {
		int idx = productIndex.getIndex(processProduct);
		if (idx < 0 || idx > scalingFactors.length)
			return 0;
		return scalingFactors[idx];
	}

	/**
	 * Get the sum of all scaling factors for the products of the process with
	 * the given ID.
	 */
	public double getScalingFactor(long processId) {
		double factor = 0;
		List<LongPair> productIds = productIndex.getProviders(processId);
		for (LongPair product : productIds) {
			int idx = productIndex.getIndex(product);
			if (idx < 0 || idx > scalingFactors.length)
				continue;
			factor += scalingFactors[idx];
		}
		return factor;
	}

	/**
	 * Returns the total result of the intervention flow with the given ID. Note
	 * that inputs have a negative value.
	 */
	public double getTotalFlowResult(long flowId) {
		int idx = flowIndex.getIndex(flowId);
		if (idx < 0 || idx >= totalFlowResults.length)
			return 0;
		return totalFlowResults[idx];
	}

	/**
	 * Returns the total result of the LCIA category with the given ID.
	 */
	public double getTotalImpactResult(long impactId) {
		if (!hasImpactResults())
			return 0;
		int idx = impactIndex.getIndex(impactId);
		if (idx < 0 || idx >= totalImpactResults.length)
			return 0;
		return totalImpactResults[idx];
	}
}
