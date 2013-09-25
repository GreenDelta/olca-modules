package org.openlca.core.math;

import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ProductSystem;
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

	private ProductSystem system;
	private ImpactMethodDescriptor impactMethod;
	private MatrixCache database;

	private SimulationResult result;
	private Inventory inventory;
	private InventoryMatrix inventoryMatrix;
	private ImpactTable impactTable;
	private ImpactMatrix impactMatrix;
	private CalculationSetup setup;

	public Simulator(CalculationSetup setup, MatrixCache database) {
		this.system = setup.getProductSystem();
		this.impactMethod = setup.getImpactMethod();
		this.database = database;
		this.setup = setup;
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
			InventorySolver solver = new InventorySolver();
			InventoryResult inventoryResult = solver.solve(inventoryMatrix,
					impactMatrix);
			result.appendFlowResults(inventoryResult.getFlowResultVector());
			if (result.hasImpactResults())
				result.appendImpactResults(inventoryResult
						.getImpactResultVector());
			return true;
		} catch (Throwable e) {
			log.trace("simulation run failed", e);
			return false;
		}
	}

	private void setUp() {
		log.trace("set up inventory");
		inventory = Calculators.createInventory(setup, database);
		inventoryMatrix = inventory.asMatrix();
		result = new SimulationResult(inventory.getFlowIndex());
		if (impactMethod != null) {
			ImpactTable impactTable = Calculators.createImpactTable(
					impactMethod, inventory.getFlowIndex(), database);
			if (impactTable.isEmpty())
				return;
			this.impactTable = impactTable;
			this.impactMatrix = impactTable.asMatrix();
			result.setImpactIndex(impactTable.getCategoryIndex());
		}
	}

}
