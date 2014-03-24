package org.openlca.core.matrix;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.matrix.cache.MatrixCache;

public class CostMatrix {

	private ProductIndex productIndex;
	private LongIndex fixCostCategoryIndex;
	private LongIndex varCostCategoryIndex;

	private IMatrix fixCostMatrix;
	private IMatrix variableCostMatrix;

	/** Returns an empty matrix. */
	public static CostMatrix empty() {
		return new CostMatrix();
	}

	public static CostMatrix build(MatrixCache matrixCache,
			IMatrixFactory<?> factory, ProductIndex productIndex) {
		return new CostMatrixBuilder(matrixCache, factory, productIndex)
				.build();
	}

	public boolean isEmpty() {
		return productIndex == null || (!hasFixCosts() && !hasVarCosts());
	}

	public boolean hasFixCosts() {
		return fixCostCategoryIndex != null && !fixCostCategoryIndex.isEmpty()
				&& fixCostMatrix != null;
	}

	public boolean hasVarCosts() {
		return varCostCategoryIndex != null && !varCostCategoryIndex.isEmpty()
				&& variableCostMatrix != null;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public IMatrix getFixCostMatrix() {
		return fixCostMatrix;
	}

	public void setFixCosts(LongIndex fixCostCategoryIndex,
			IMatrix fixCostMatrix) {
		this.fixCostCategoryIndex = fixCostCategoryIndex;
		this.fixCostMatrix = fixCostMatrix;
	}

	public IMatrix getVariableCostMatrix() {
		return variableCostMatrix;
	}

	public void setVariableCosts(LongIndex varCostCategoryIndex,
			IMatrix variableCostMatrix) {
		this.variableCostMatrix = variableCostMatrix;
		this.varCostCategoryIndex = varCostCategoryIndex;
	}

	public LongIndex getFixCostCategoryIndex() {
		return fixCostCategoryIndex;
	}

	public LongIndex getVarCostCategoryIndex() {
		return varCostCategoryIndex;
	}

}
