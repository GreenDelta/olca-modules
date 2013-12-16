package org.openlca.core.matrix;

import org.openlca.core.math.IMatrix;

public class ImpactMatrix {

	private LongIndex categoryIndex;
	private FlowIndex flowIndex;
	private IMatrix factorMatrix;

	public boolean isEmpty() {
		return flowIndex == null || flowIndex.size() == 0
				|| categoryIndex == null || categoryIndex.size() == 0
				|| factorMatrix == null;
	}

	public LongIndex getCategoryIndex() {
		return categoryIndex;
	}

	public void setCategoryIndex(LongIndex categoryIndex) {
		this.categoryIndex = categoryIndex;
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public void setFlowIndex(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
	}

	public IMatrix getFactorMatrix() {
		return factorMatrix;
	}

	public void setFactorMatrix(IMatrix factorMatrix) {
		this.factorMatrix = factorMatrix;
	}

}
