package org.openlca.core.results;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrix.LongPair;

/**
 * A full result extends the base and contribution result by providing
 * additionally all calculated upstream-results for intervention flows and LCIA
 * categories for each single process-product in the system.
 */
public interface IFullResult extends IContributionResult {

	/**
	 * Get the upstream flow results in a matrix where the flows are mapped to
	 * the rows and the process-products to the columns. Inputs have negative
	 * values here.
	 */
	IMatrix getUpstreamFlowResults();

	/**
	 * Get the upstream flow result of the flow with the given ID for the given
	 * process-product. Inputs have negative values here.
	 */
	double getUpstreamFlowResult(LongPair processProduct, long flowId);

	/**
	 * Get the sum of the upstream flow results of the flow with the given ID
	 * for all products of the process with the given ID. Inputs have negative
	 * values here.
	 */
	double getUpstreamFlowResult(long processId, long flowId);

	/**
	 * Get the upstream LCIA category results in a matrix where the LCIA
	 * categories are mapped to the rows and the process-products to the
	 * columns.
	 */
	IMatrix getUpstreamImpactResults();

	/**
	 * Get the upstream LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 */
	double getUpstreamImpactResult(LongPair processProduct, long impactId);

	/**
	 * Get the sum of the upstream LCIA category results of the LCIA category
	 * with the given ID for all products of the process with the given ID.
	 */
	double getUpstreamImpactResult(long processId, long impactId);

}
