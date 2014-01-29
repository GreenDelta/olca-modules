package org.openlca.core.results;

/**
 * The simplest kind of result of a calculated product system. It contains the
 * total interventions and optionally the total impact assessment results of the
 * product system. This result type is suitable for Monte-Carlo-Simulations or
 * other quick calculations.
 */
public class SimpleResult extends BaseResult {

	protected double[] totalFlowResults;
	protected double[] totalImpactResults;

	public void setTotalFlowResults(double[] totalFlowResults) {
		this.totalFlowResults = totalFlowResults;
	}

	/**
	 * Get the total results of all intervention flows. Note that inputs have a
	 * negative value.
	 */
	public double[] getTotalFlowResults() {
		return totalFlowResults;
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

	public void setTotalImpactResults(double[] totalImpactResults) {
		this.totalImpactResults = totalImpactResults;
	}

	/**
	 * Get the total results of all LCIA categories.
	 */
	public double[] getTotalImpactResults() {
		return totalImpactResults;
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
