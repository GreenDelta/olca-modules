package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrices.ImpactMatrix;
import org.openlca.core.matrices.Inventory;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase database;

	public SystemCalculator(IDatabase database) {
		this.database = database;
	}

	public InventoryResult solve(ProductSystem system) {
		return solve(system, null);
	}

	public InventoryResult solve(ProductSystem system,
			ImpactMethodDescriptor method) {
		log.trace("solve product system {}", system);
		log.trace("create inventory");
		Inventory inventory = Calculators.createInventory(system, database);
		log.trace("solve invenotory");
		InventorySolver solver = new InventorySolver();
		if (method == null)
			return solver.solve(inventory);
		else {
			ImpactMatrix impactMatrix = Calculators.createImpactMatrix(method,
					inventory.getFlowIndex(), database);
			return solver.solve(inventory, impactMatrix);
		}
	}

	public AnalysisResult analyse(ProductSystem system) {
		return analyse(system, null);
	}

	public AnalysisResult analyse(ProductSystem system,
			ImpactMethodDescriptor method) {
		log.trace("analyse product system {}", system);
		log.trace("create inventory");
		Inventory inventory = Calculators.createInventory(system, database);
		log.trace("analyse inventory");
		InventorySolver solver = new InventorySolver();
		if (method == null)
			return solver.analyse(inventory);
		else {
			ImpactMatrix impactMatrix = Calculators.createImpactMatrix(method,
					inventory.getFlowIndex(), database);
			return solver.analyse(inventory, impactMatrix);
		}
	}

}
