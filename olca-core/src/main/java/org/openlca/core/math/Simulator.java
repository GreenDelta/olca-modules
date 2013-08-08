package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrices.ImpactMatrix;
import org.openlca.core.matrices.Inventory;
import org.openlca.core.matrices.InventoryMatrix;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.SimulationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A calculator for Monte-Carlo-Simulations.
 * 
 * TODO: allocation-method
 */
public class Simulator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductSystem system;
	private ImpactMethodDescriptor impactMethod;
	private IDatabase database;

	private SimulationResult result;
	private ImpactMatrix impactMatrix;
	private Inventory inventory;
	private InventoryMatrix matrix;

	public Simulator(ProductSystem system, IDatabase database) {
		this(system, null, database);
	}

	public Simulator(ProductSystem system, ImpactMethodDescriptor impactMethod,
			IDatabase database) {
		this.system = system;
		this.impactMethod = impactMethod;
		this.database = database;
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
		if (inventory == null || matrix == null)
			setUp();
		try {
			log.trace("next simulation run");
			inventory.getInterventionMatrix().simulate(
					matrix.getInterventionMatrix());
			inventory.getTechnologyMatrix().simulate(
					matrix.getTechnologyMatrix());
			InventorySolver solver = new InventorySolver();
			InventoryResult inventoryResult = solver
					.solve(matrix, impactMatrix);
			result.appendFlowResults(inventoryResult.getFlowResults());
			if (result.hasImpactResults())
				result.appendImpactResults(inventoryResult.getImpactResults());
			return true;
		} catch (Throwable e) {
			log.trace("simulation run failed", e);
			return false;
		}
	}

	private void setUp() {
		log.trace("set up inventory");
		inventory = Calculators.createInventory(system, database);
		if (impactMethod != null)
			impactMatrix = Calculators.createImpactMatrix(impactMethod,
					inventory.getFlowIndex(), database);
		matrix = new InventoryMatrix();
		matrix.setFlowIndex(inventory.getFlowIndex());
		matrix.setProductIndex(inventory.getProductIndex());
		IMatrix techMatrix = inventory.getTechnologyMatrix().createRealMatrix();
		matrix.setTechnologyMatrix(techMatrix);
		IMatrix enviMatrix = inventory.getInterventionMatrix()
				.createRealMatrix();
		matrix.setInterventionMatrix(enviMatrix);
		result = new SimulationResult(inventory.getFlowIndex());
		if (impactMatrix != null)
			result.setImpactIndex(impactMatrix.getCategoryIndex());
	}

}
