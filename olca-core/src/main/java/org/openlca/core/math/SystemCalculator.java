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

	public SystemCalculator(MatrixCache cache, IMatrixSolver solver) {
		this.matrixCache = cache;
		this.solver = solver;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate product system - simple result");
		InventoryMatrix inventory = makeInventory(setup);
		LcaCalculator calculator = new LcaCalculator(solver);
		if (setup.getImpactMethod() == null)
			return calculator.calculateSimple(inventory);
		else {
			ImpactMatrix impactMatrix = makeImpactMatrix(setup, inventory);
			return calculator.calculateSimple(inventory, impactMatrix);
		}
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		InventoryMatrix inventory = makeInventory(setup);
		LcaCalculator calculator = new LcaCalculator(solver);
		if (setup.getImpactMethod() == null)
			return calculator.calculateContributions(inventory);
		else {
			ImpactMatrix impactMatrix = makeImpactMatrix(setup, inventory);
			return calculator.calculateContributions(inventory, impactMatrix);
		}
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		InventoryMatrix inventory = makeInventory(setup);
		LcaCalculator calculator = new LcaCalculator(solver);
		if (setup.getImpactMethod() == null)
			return calculator.calculateFull(inventory);
		else {
			ImpactMatrix impactMatrix = makeImpactMatrix(setup, inventory);
			return calculator.calculateFull(inventory, impactMatrix);
		}
	}

	private InventoryMatrix makeInventory(CalculationSetup setup) {
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		IDatabase db = matrixCache.getDatabase();
		ParameterTable parameterTable = Calculators.createParameterTable(db,
				setup, inventory);
		FormulaInterpreter interpreter = parameterTable.createInterpreter();
		InventoryMatrix matrix = inventory.createMatrix(
				solver.getMatrixFactory(), interpreter);
		return matrix;
	}

	private ImpactMatrix makeImpactMatrix(CalculationSetup setup,
			InventoryMatrix inventory) {
		ImpactTable impactTable = Calculators.createImpactTable(
				setup.getImpactMethod(), inventory.getFlowIndex(), matrixCache);
		return impactTable.asMatrix(solver.getMatrixFactory());
	}
}
