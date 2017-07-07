package org.openlca.core.matrix;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Contains all the information of the inventory of a complete product system.
 */
public class Inventory {

	public TechIndex productIndex;
	public FlowIndex flowIndex;
	public ExchangeMatrix technologyMatrix;
	public ExchangeMatrix interventionMatrix;
	public AllocationMethod allocationMethod;

	public static Inventory build(MatrixCache matrixCache,
			TechIndex productIndex, AllocationMethod allocationMethod) {
		return new InventoryBuilder(matrixCache, productIndex, allocationMethod)
				.build();
	}

	public boolean isEmpty() {
		return productIndex == null || productIndex.size() == 0
				|| flowIndex == null || flowIndex.isEmpty()
				|| technologyMatrix == null || technologyMatrix.isEmpty()
				|| interventionMatrix == null || interventionMatrix.isEmpty();
	}

	public InventoryMatrix createMatrix(IMatrixSolver solver) {
		return createMatrix(solver, null);
	}

	public InventoryMatrix createMatrix(IMatrixSolver solver,
			FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		InventoryMatrix matrix = new InventoryMatrix();
		matrix.flowIndex = flowIndex;
		matrix.productIndex = productIndex;
		IMatrix enviMatrix = interventionMatrix.createRealMatrix(solver);
		matrix.interventionMatrix = enviMatrix;
		IMatrix techMatrix = technologyMatrix.createRealMatrix(solver);
		matrix.technologyMatrix = techMatrix;
		return matrix;
	}

	/**
	 * Re-evaluates the parameters and formulas in the inventory (because they
	 * may changed), generates new values for the entries that have an
	 * uncertainty distribution and set these values to the entries of the given
	 * matrix. The given matrix and this inventory have to match exactly in size
	 * (so normally you first call createMatrix and than simulate).
	 */
	public void simulate(InventoryMatrix matrix, FormulaInterpreter interpreter) {
		evalFormulas(interpreter);
		if (technologyMatrix != null)
			technologyMatrix.simulate(matrix.technologyMatrix);
		if (interventionMatrix != null)
			interventionMatrix.simulate(matrix.interventionMatrix);
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		if (technologyMatrix != null)
			technologyMatrix.eval(interpreter);
		if (interventionMatrix != null)
			interventionMatrix.eval(interpreter);
	}

}
