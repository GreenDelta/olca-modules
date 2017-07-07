package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final MatrixCache matrixCache;
	private final IMatrixSolver solver;

	public SystemCalculator(MatrixCache cache, IMatrixSolver solver) {
		this.matrixCache = cache;
		this.solver = solver;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate product system - simple result");
		return calculator(setup).calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		return calculator(setup).calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		return calculator(setup).calculateFull();
	}

	private LcaCalculator calculator(CalculationSetup setup) {
		IDatabase db = matrixCache.getDatabase();
		Inventory inventory = DataStructures.createInventory(setup, matrixCache);
		ParameterTable parameterTable = DataStructures.createParameterTable(db,
				setup, inventory);
		FormulaInterpreter interpreter = parameterTable.createInterpreter();
		InventoryMatrix inventoryMatrix = inventory.createMatrix(
				solver, interpreter);
		LcaCalculator calculator = new LcaCalculator(solver, inventoryMatrix);
		if (setup.impactMethod != null) {
			ImpactTable impactTable = ImpactTable.build(matrixCache,
					setup.impactMethod.getId(), inventory.flowIndex);
			ImpactMatrix impactMatrix = impactTable.createMatrix(
					solver, interpreter);
			calculator.setImpactMatrix(impactMatrix);
		}
		if (setup.withCosts) {
			CostVector costVector = CostVector.build(inventory, db);
			if (!costVector.isEmpty())
				calculator.setCostVector(costVector);
		}
		return calculator;
	}
}
