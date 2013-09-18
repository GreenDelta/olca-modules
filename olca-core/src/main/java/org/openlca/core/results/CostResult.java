package org.openlca.core.results;

import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;

/**
 * Represents the result of LCC calculation. Note that the fix-cost and
 * variable-cost indices and results can be <code>null</code>. You can use the
 * CostResults class to generate results for the views.
 */
public class CostResult {

	private ProductIndex productIndex;
	private LongIndex fixCostCategoryIndex;
	private LongIndex varCostCategoryIndex;
	private double[] fixCostResults;
	private double[] varCostResults;

	public boolean hasFixCostResults() {
		return fixCostCategoryIndex != null && !fixCostCategoryIndex.isEmpty()
				&& fixCostResults != null && fixCostResults.length > 0;
	}

	public boolean hasVarCostResults() {
		return varCostCategoryIndex != null && !varCostCategoryIndex.isEmpty()
				&& varCostResults != null && varCostResults.length > 0;
	}

	public ProductIndex getProductIndex() {
		return productIndex;
	}

	public void setProductIndex(ProductIndex productIndex) {
		this.productIndex = productIndex;
	}

	public LongIndex getFixCostCategoryIndex() {
		return fixCostCategoryIndex;
	}

	public void setFixCostCategoryIndex(LongIndex fixCostCategoryIndex) {
		this.fixCostCategoryIndex = fixCostCategoryIndex;
	}

	public LongIndex getVarCostCategoryIndex() {
		return varCostCategoryIndex;
	}

	public void setVarCostCategoryIndex(LongIndex varCostCategoryIndex) {
		this.varCostCategoryIndex = varCostCategoryIndex;
	}

	public double[] getFixCostResults() {
		return fixCostResults;
	}

	public void setFixCostResults(double[] fixCostResults) {
		this.fixCostResults = fixCostResults;
	}

	public double[] getVarCostResults() {
		return varCostResults;
	}

	public void setVarCostResults(double[] varCostResults) {
		this.varCostResults = varCostResults;
	}

}
