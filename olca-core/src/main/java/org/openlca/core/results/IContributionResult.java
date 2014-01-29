package org.openlca.core.results;

import org.openlca.core.math.IMatrix;
import org.openlca.core.matrix.LongPair;

/**
 * A contribution result extends a base result and contains all single
 * contributions of processes and products to the overall inventory and impact
 * assessment results. Additionally, it contains the contributions of the single
 * inventory flows to impact category results.
 */
public interface IContributionResult extends IBaseResult {

	/**
	 * Get the scaling factors for the process-product columns.
	 */
	double[] getScalingFactors();

	/**
	 * Get the scaling factor of the given process-product.
	 */
	double getScalingFactor(LongPair processProduct);

	/**
	 * Get the sum of all scaling factors for the products of the process with
	 * the given ID.
	 */
	double getScalingFactor(long processId);

	/**
	 * Get the single flow results in a matrix where the flows are mapped to the
	 * rows and the process-products to the columns. Inputs have negative values
	 * here.
	 */
	IMatrix getSingleFlowResults();

	/**
	 * Get the single flow result of the flow with the given ID for the given
	 * process-product. Inputs have negative values here.
	 */
	double getSingleFlowResult(LongPair processProduct, long flowId);

	/**
	 * Get the sum of the single flow results of the flow with the given ID for
	 * all products of the process with the given ID. Inputs have negative
	 * values here.
	 */
	double getSingleFlowResult(long processId, long flowId);

	/**
	 * Get the single LCIA category results in a matrix where the LCIA
	 * categories are mapped to the rows and the process-products to the
	 * columns.
	 */
	IMatrix getSingleImpactResults();

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 */
	double getSingleImpactResult(LongPair processProduct, long impactId);

	/**
	 * Get the sum of the single LCIA category results of the LCIA category with
	 * the given ID for all products of the process with the given ID.
	 */
	double getSingleImpactResult(long processId, long impactId);

	/**
	 * Get the single LCIA category result for each intervention flow in a
	 * matrix where the LCIA categories are mapped to the rows and the
	 * intervention flows to the columns.
	 */
	IMatrix getSingleFlowImpacts();

	/**
	 * Get the single LCIA category result of the LCIA category with the given
	 * ID for the given process-product.
	 */
	double getSingleFlowImpact(long flowId, long impactId);

	/**
	 * Get the contributions of the product-links in the scaled product system.
	 */
	LinkContributions getLinkContributions();

}
