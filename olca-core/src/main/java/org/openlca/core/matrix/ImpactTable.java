package org.openlca.core.matrix;

import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.expressions.FormulaInterpreter;

/**
 * The ImpactTable is the corresponding data type to the Inventory type but
 * wraps a matrix of impact assessment factors.
 */
public class ImpactTable {

	private LongIndex categoryIndex;
	private FlowIndex flowIndex;
	private ImpactFactorMatrix factorMatrix;

	public static ImpactTable build(MatrixCache cache, long impactMethodId,
			FlowIndex flowIndex) {
		return new ImpactTableBuilder(cache, impactMethodId, flowIndex).build();
	}

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

	public ImpactMatrix createMatrix(IMatrixFactory<?> factory) {
		return createMatrix(factory, null);
	}

	public ImpactMatrix createMatrix(IMatrixFactory<?> factory,
			FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		ImpactMatrix matrix = new ImpactMatrix();
		matrix.setCategoryIndex(categoryIndex);
		if (factorMatrix != null)
			matrix.setFactorMatrix(factorMatrix.createRealMatrix(factory));
		matrix.setFlowIndex(flowIndex);
		return matrix;
	}

	/**
	 * Re-evaluates the parameters and formulas in the impact factor table
	 * (because they may changed), generates new values for the entries that
	 * have an uncertainty distribution and set these values to the entries of
	 * the given matrix. The given matrix and this impact table have to match
	 * exactly in size (so normally you first call createMatrix and than
	 * simulate).
	 */
	public void simulate(ImpactMatrix matrix, FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		if (factorMatrix != null)
			factorMatrix.simulate(matrix.getFactorMatrix());
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		if (factorMatrix != null)
			factorMatrix.eval(interpreter);
	}

}
