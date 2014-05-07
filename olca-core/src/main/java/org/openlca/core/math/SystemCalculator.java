package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.cache.MatrixCache;
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

	private InventoryMatrix inventoryMatrix;
	private ImpactMatrix impactMatrix;

	public SystemCalculator(MatrixCache cache, IMatrixSolver solver) {
		this.matrixCache = cache;
		this.solver = solver;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate product system - simple result");
		doSetUp(setup);
		LcaCalculator calculator = new LcaCalculator(solver);
		return calculator.calculateSimple(inventoryMatrix, impactMatrix);
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		doSetUp(setup);
		LcaCalculator calculator = new LcaCalculator(solver);
		return calculator.calculateContributions(inventoryMatrix, impactMatrix);
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		doSetUp(setup);
		LcaCalculator calculator = new LcaCalculator(solver);
		return calculator.calculateFull(inventoryMatrix, impactMatrix);
	}

	private void doSetUp(CalculationSetup setup) {
		IDatabase db = matrixCache.getDatabase();
		Inventory inventory = DataStructures.createInventory(setup, matrixCache);
		ParameterTable parameterTable = DataStructures.createParameterTable(db,
				setup, inventory);
		FormulaInterpreter interpreter = parameterTable.createInterpreter();
		this.inventoryMatrix = inventory.createMatrix(
				solver.getMatrixFactory(), interpreter);
		if (setup.getImpactMethod() != null) {
			ImpactTable impactTable = ImpactTable.build(matrixCache, setup
					.getImpactMethod().getId(), inventory.getFlowIndex());
			this.impactMatrix = impactTable.createMatrix(
					solver.getMatrixFactory(), interpreter);
		}
	}
}
