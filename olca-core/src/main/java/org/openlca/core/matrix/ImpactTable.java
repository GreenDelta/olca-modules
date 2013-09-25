package org.openlca.core.matrix;

public class ImpactTable {

	private LongIndex categoryIndex;
	private FlowIndex flowIndex;
	private ImpactFactorMatrix factorMatrix;

	public boolean isEmpty() {
		return categoryIndex == null || categoryIndex.isEmpty()
				|| flowIndex == null || flowIndex.isEmpty()
				|| factorMatrix == null || factorMatrix.isEmpty();
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

	public ImpactFactorMatrix getFactorMatrix() {
		return factorMatrix;
	}

	public void setFactorMatrix(ImpactFactorMatrix factorMatrix) {
		this.factorMatrix = factorMatrix;
	}

	public ImpactMatrix asMatrix() {
		ImpactMatrix matrix = new ImpactMatrix();
		matrix.setCategoryIndex(categoryIndex);
		if (factorMatrix != null)
			matrix.setFactorMatrix(factorMatrix.createRealMatrix());
		matrix.setFlowIndex(flowIndex);
		return matrix;
	}

}
