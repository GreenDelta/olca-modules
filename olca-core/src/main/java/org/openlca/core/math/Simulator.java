package org.openlca.core.math;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.SimulationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A calculator for Monte-Carlo-Simulations.
 */
public class Simulator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ImpactMethodDescriptor impactMethod;
	private MatrixCache database;
	private final IMatrixFactory<?> factory;
	private final IMatrixSolver matrixSolver;

	private SimulationResult result;
	private Inventory inventory;
	private InventoryMatrix inventoryMatrix;
	private ImpactTable impactTable;
	private ImpactMatrix impactMatrix;
	private CalculationSetup setup;

	public Simulator(CalculationSetup setup, MatrixCache database,
			IMatrixSolver solver) {
		this.impactMethod = setup.getImpactMethod();
		this.database = database;
		this.setup = setup;
		this.factory = solver.getMatrixFactory();
		this.matrixSolver = solver;
	}

	public SimulationResult getResult() {
		return result;
	}

	/**
	 * Generates random numbers and calculates the product system. Returns true
	 * if the calculation was successfully done, otherwise false (this is the
	 * case when the resulting matrix is singular).
	 */
	public boolean nextRun() {
		if (inventory == null || inventoryMatrix == null)
			setUp();
		try {
			log.trace("next simulation run");
			inventory.getInterventionMatrix().simulate(
					inventoryMatrix.getInterventionMatrix());
			inventory.getTechnologyMatrix().simulate(
					inventoryMatrix.getTechnologyMatrix());
			if (impactMatrix != null)
				impactTable.getFactorMatrix().simulate(
						impactMatrix.getFactorMatrix());
			InventoryCalculator solver = new InventoryCalculator(matrixSolver);
			InventoryResult inventoryResult = solver.solve(inventoryMatrix,
					impactMatrix);
			appendResults(inventoryResult);
			return true;
		} catch (Throwable e) {
			log.trace("simulation run failed", e);
			return false;
		}
	}

	private void appendResults(InventoryResult inventoryResult) {
		FlowIndex flowIndex = result.getFlowIndex();
		double[] flowResults = new double[flowIndex.size()];
		for (long flowId : flowIndex.getFlowIds()) {
			int idx = flowIndex.getIndex(flowId);
			// get result adopts the sign for input flow results
			flowResults[idx] = inventoryResult.getFlowResult(flowId);
		}
		result.appendFlowResults(flowResults);
		if (result.hasImpactResults())
			result.appendImpactResults(inventoryResult.getImpactResultVector());
	}

	private void setUp() {
		log.trace("set up inventory");
		inventory = Calculators.createInventory(setup, database);
		inventoryMatrix = inventory.asMatrix(factory);
		result = new SimulationResult(inventory.getFlowIndex());
		if (impactMethod != null) {
			ImpactTable impactTable = Calculators.createImpactTable(
					impactMethod, inventory.getFlowIndex(), database);
			if (impactTable.isEmpty())
				return;
			this.impactTable = impactTable;
			this.impactMatrix = impactTable.asMatrix(factory);
			result.setImpactIndex(impactTable.getCategoryIndex());
		}
	}

}
