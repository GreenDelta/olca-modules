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

	public IMatrix createMatrix(IMatrixSolver solver) {
		return createMatrix(solver, null);
	}

	public IMatrix createMatrix(IMatrixSolver solver,
			FormulaInterpreter interpreter) {
		if (factorMatrix == null)
			return null;
		evalFormulas(interpreter);
		return (IMatrix) factorMatrix.createRealMatrix(solver);
	}

	/**
	 * Re-evaluates the parameters and formulas in the impact factor table
	 * (because they may changed), generates new values for the entries that
	 * have an uncertainty distribution and set these values to the entries of
	 * the given matrix. The given matrix and this impact table have to match
	 * exactly in size (so normally you first call createMatrix and than
	 * simulate).
	 */
	public void simulate(IMatrix matrix, FormulaInterpreter interpreter) {
		if (matrix == null)
			return;
		evalFormulas(interpreter);
		if (factorMatrix != null) {
			factorMatrix.simulate(matrix);
		}
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null || factorMatrix == null)
			return;
		factorMatrix.eval(interpreter);
	}

}
