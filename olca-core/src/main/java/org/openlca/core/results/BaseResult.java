package org.openlca.core.results;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;

public abstract class BaseResult {

	/**
	 * The product index maps the process-product-IDs from the technosphere to
	 * column and row indices of the matrices and vectors of the mathematical
	 * model.
	 */
	public ProductIndex productIndex;

	/**
	 * The flow index which maps the flow-IDs from the interventions to column
	 * and row indices of the matrices and vectors of the mathematical model.
	 */
	public FlowIndex flowIndex;

	/**
	 * The impact category index maps IDs of impact categories to column and row
	 * indices of the matrices and vectors of the mathematical model.
	 */
	public LongIndex impactIndex;

	/**
	 * The cost index maps IDs of cost categories to row and column indices of
	 * the matrices and vectors of the mathematical model. As cost categories
	 * are optional there is a special category with ID=0L contained in the
	 * index which means 'no specific category'.
	 */
	public LongIndex costIndex;

	/**
	 * Returns true if this result contains an LCIA result.
	 */
	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

	/**
	 * Returns true if this result contains an LCC result.
	 */
	public boolean hasCostResults() {
		return costIndex != null && !costIndex.isEmpty();
	}

}
