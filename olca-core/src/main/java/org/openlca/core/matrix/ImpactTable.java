package org.openlca.core.matrix;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.expressions.FormulaInterpreter;

/**
 * The ImpactTable is the corresponding data type to the Inventory type but
 * wraps a matrix of impact assessment factors.
 */
public class ImpactTable {

	public LongIndex categoryIndex;
	public FlowIndex flowIndex;
	public ImpactFactorMatrix factorMatrix;

	public static ImpactTable build(MatrixCache cache, long impactMethodId,
			FlowIndex flowIndex) {
		return new ImpactTableBuilder(cache, impactMethodId, flowIndex).build();
	}

	public boolean isEmpty() {
		return categoryIndex == null || categoryIndex.isEmpty()
				|| flowIndex == null || flowIndex.isEmpty()
				|| factorMatrix == null || factorMatrix.isEmpty();
	}

	public ImpactMatrix createMatrix(IMatrixSolver solver) {
		return createMatrix(solver, null);
	}

	public ImpactMatrix createMatrix(IMatrixSolver solver,
			FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		ImpactMatrix matrix = new ImpactMatrix();
		matrix.categoryIndex = categoryIndex;
		if (factorMatrix != null)
			matrix.factorMatrix = (IMatrix) factorMatrix.createRealMatrix(solver);
		matrix.flowIndex = flowIndex;
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
			factorMatrix.simulate(matrix.factorMatrix);
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		if (factorMatrix != null)
			factorMatrix.eval(interpreter);
	}

}
