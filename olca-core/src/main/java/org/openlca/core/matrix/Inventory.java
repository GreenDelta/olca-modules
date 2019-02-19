package org.openlca.core.matrix;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Contains all the information of the inventory of a complete product system.
 */
public class Inventory {

	public TechIndex techIndex;
	public FlowIndex flowIndex;
	public ExchangeMatrix technologyMatrix;
	public ExchangeMatrix interventionMatrix;
	public AllocationMethod allocationMethod;

	public static Inventory build(MatrixCache mcache,
			TechIndex techIndex, AllocationMethod allocation) {
		return new InventoryBuilder(
				mcache, techIndex, allocation).build();
	}

	public boolean isEmpty() {
		return techIndex == null || techIndex.size() == 0
				|| flowIndex == null || flowIndex.isEmpty()
				|| technologyMatrix == null || technologyMatrix.isEmpty()
				|| interventionMatrix == null || interventionMatrix.isEmpty();
	}

	public MatrixData createMatrix(IMatrixSolver solver) {
		return createMatrix(solver, null);
	}

	public MatrixData createMatrix(IMatrixSolver solver,
			FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		MatrixData data = new MatrixData();
		data.enviIndex = flowIndex;
		data.techIndex = techIndex;
		data.enviMatrix = interventionMatrix.createRealMatrix(solver);
		data.techMatrix = technologyMatrix.createRealMatrix(solver);
		return data;
	}

	/**
	 * Re-evaluates the parameters and formulas in the inventory (because they
	 * may changed), generates new values for the entries that have an
	 * uncertainty distribution and set these values to the entries of the given
	 * matrix. The given matrix and this inventory have to match exactly in size
	 * (so normally you first call createMatrix and than simulate).
	 */
	public void simulate(MatrixData data, FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		if (technologyMatrix != null) {
			technologyMatrix.simulate(data.techMatrix);
		}
		if (interventionMatrix != null) {
			interventionMatrix.simulate(data.enviMatrix);
		}
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		if (technologyMatrix != null) {
			technologyMatrix.eval(interpreter);
		}
		if (interventionMatrix != null) {
			interventionMatrix.eval(interpreter);
		}
	}
}
