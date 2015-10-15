package org.openlca.core.results;

/**
 * The simplest kind of result of a calculated product system. It contains the
 * total interventions and optionally the total impact assessment results of the
 * product system. This result type is suitable for Monte-Carlo-Simulations or
 * other quick calculations.
 */
public class SimpleResult extends BaseResult {

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
	 * The total results of all cost categories.
	 */
	public double[] totalCostResults;

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

	/**
	 * Returns the total result of the cost category with the given ID.
	 */
	public double getTotalCostResult(long costId) {
		if (!hasCostResults())
			return 0;
		int idx = costIndex.getIndex(costId);
		if (idx < 0 || idx >= totalCostResults.length)
			return 0;
		return totalCostResults[idx];
	}
}
