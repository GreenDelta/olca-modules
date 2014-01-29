package org.openlca.core.results;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;

/**
 * A base result is the simplest kind of result of a calculated product system.
 * It contains the total interventions and optionally the total impact
 * assessment results of the product system. This result type is suitable for
 * Monte-Carlo-Simulations or other quick calculations.
 */
public interface IBaseResult {

	/**
	 * Get the product index which maps the process-product-IDs from the
	 * technosphere to column and row indices of the matrices and vectors of the
	 * mathematical model.
	 */
	ProductIndex getProductIndex();

	/**
	 * Get the flow index which maps the flow-IDs from the interventions to
	 * column and row indices of the matrices and vectors of the mathematical
	 * model.
	 */
	FlowIndex getFlowIndex();

	/**
	 * Get the impact category index which maps IDs of impact categories to
	 * column and row indices of the matrices and vectors of the mathematical
	 * model.
	 */
	LongIndex getImpactIndex();

	/**
	 * Returns true if this result contains an LCIA result.
	 */
	boolean hasImpactResults();

	/**
	 * Get the total results of all intervention flows. Note that inputs have a
	 * negative value.
	 */
	double[] getTotalFlowResults();

	/**
	 * Returns the total result of the intervention flow with the given ID. Note
	 * that inputs have a negative value.
	 */
	double getTotalFlowResult(long flowId);

	/**
	 * Get the total results of all LCIA categories.
	 */
	double[] getTotalImpactResults();

	/**
	 * Returns the total result of the LCIA category with the given ID.
	 */
	double getTotalImpactResult(long impactId);

}
