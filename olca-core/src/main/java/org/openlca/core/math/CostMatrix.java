package org.openlca.core.math;

import org.openlca.core.model.CostCategory;

public class CostMatrix {

	private IMatrix technologyMatrix;
	private IMatrix costMatrix;
	private ProductIndex productIndex;
	private Index<CostCategory> costCategoryIndex;

	/**
	 * Creates a cost matrix with no results, this {@link #isEmpty()} returns
	 * true.
	 */
	public static CostMatrix empty() {
		return new CostMatrix();
	}

	/**
	 * Indicates that no cost entries are stored in the matrix and thus no cost
	 * result can be calculated.
	 */
	public boolean isEmpty() {
		return costCategoryIndex == null || costCategoryIndex.isEmpty();
	}

	public IMatrix getTechnologyMatrix() {
		return technologyMatrix;
	}

	public void setTechnologyMatrix(IMatrix technologyMatrix) {
		this.technologyMatrix = technologyMatrix;
	}

	public IMatrix getCostMatrix() {
		return costMatrix;
	}

	public void setCostMatrix(IMatrix costMatrix) {
		this.costMatrix = costMatrix;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public Index<CostCategory> getCostCategoryIndex() {
		return costCategoryIndex;
	}

	public void setCostCategoryIndex(Index<CostCategory> costCategoryIndex) {
		this.costCategoryIndex = costCategoryIndex;
	}

}
