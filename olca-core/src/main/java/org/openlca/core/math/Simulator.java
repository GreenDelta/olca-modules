package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimulationResult;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A calculator for Monte-Carlo-Simulations.
 */
public class Simulator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ImpactMethodDescriptor impactMethod;
	private MatrixCache cache;
	private final IMatrixSolver matrixSolver;

	private SimulationResult result;
	private Inventory inventory;
	private ParameterTable parameterTable;
	private InventoryMatrix inventoryMatrix;
	private ImpactTable impactTable;
	private ImpactMatrix impactMatrix;
	private CalculationSetup setup;

	public Simulator(CalculationSetup setup, MatrixCache cache,
			IMatrixSolver solver) {
		this.impactMethod = setup.impactMethod;
		this.cache = cache;
		this.setup = setup;
		this.matrixSolver = solver;
	}

	public SimulationResult getResult() {
		return result;
	}

	/**
	 * Generates random numbers and calculates the product system. Returns the
	 * simulation result if the calculation in this run finished without errors,
	 * otherwise <code>null</code> is returned (e.g. the resulting matrix was
	 * singular). The returned result is appended to the result of the simulator
	 * (which you get via {@link #getResult()}, so it does not need to (and
	 * should not) be cached.
	 */
	public SimpleResult nextRun() {
		if (inventory == null || inventoryMatrix == null)
			setUp();
		try {
			log.trace("next simulation run");
			FormulaInterpreter interpreter = parameterTable.simulate();
			inventory.simulate(inventoryMatrix, interpreter);
			LcaCalculator solver = new LcaCalculator(matrixSolver,
					inventoryMatrix);
			if (impactMatrix != null) {
				impactTable.simulate(impactMatrix, interpreter);
				solver.setImpactMatrix(impactMatrix);
			}
			SimpleResult result = solver.calculateSimple();
			appendResults(result);
			return result;
		} catch (Throwable e) {
			log.trace("simulation run failed", e);
			return null;
		}
	}

	private void appendResults(SimpleResult result) {
		this.result.appendFlowResults(result.totalFlowResults);
		if (this.result.hasImpactResults())
			this.result.appendImpactResults(result.totalImpactResults);
	}

	private void setUp() {
		log.trace("set up inventory");
		inventory = DataStructures.createInventory(setup, cache);
		parameterTable = DataStructures.createParameterTable(
				cache.getDatabase(),
				setup, inventory);
		inventoryMatrix = inventory.createMatrix(matrixSolver);
		result = new SimulationResult();
		result.productIndex = inventory.productIndex;
		result.flowIndex = inventory.flowIndex;
		if (impactMethod != null) {
			ImpactTable impactTable = ImpactTable.build(cache,
					impactMethod.getId(), inventory.flowIndex);
			if (impactTable.isEmpty()) {
				return;
			}
			this.impactTable = impactTable;
			this.impactMatrix = impactTable.createMatrix(matrixSolver);
			result.impactIndex = impactTable.categoryIndex;
		}
	}
}
