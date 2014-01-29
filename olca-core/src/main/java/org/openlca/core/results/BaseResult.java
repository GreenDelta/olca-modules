package org.openlca.core.results;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;

public abstract class BaseResult {

	protected ProductIndex productIndex;
	protected FlowIndex flowIndex;
	protected LongIndex impactIndex;

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	/**
	 * Get the product index which maps the process-product-IDs from the
	 * technosphere to column and row indices of the matrices and vectors of the
	 * mathematical model.
	 */
	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	/**
	 * Get the flow index which maps the flow-IDs from the interventions to
	 * column and row indices of the matrices and vectors of the mathematical
	 * model.
	 */
	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public void setImpactIndex(LongIndex impactIndex) {
		this.impactIndex = impactIndex;
	}

	/**
	 * Get the impact category index which maps IDs of impact categories to
	 * column and row indices of the matrices and vectors of the mathematical
	 * model.
	 */
	public LongIndex getImpactIndex() {
		return impactIndex;
	}

	/**
	 * Returns true if this result contains an LCIA result.
	 */
	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

}
