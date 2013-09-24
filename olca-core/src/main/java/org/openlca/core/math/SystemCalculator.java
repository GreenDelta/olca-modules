package org.openlca.core.math;

import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.results.AnalysisResult;
import org.openlca.core.results.InventoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final MatrixCache matrixCache;

	public SystemCalculator(MatrixCache database) {
		this.matrixCache = database;
	}

	public InventoryResult solve(CalculationSetup setup) {
		log.trace("solve product system - build inventory");
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		log.trace("solve inventory");
		InventorySolver solver = new InventorySolver();
		if (setup.getImpactMethod() == null)
			return solver.solve(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return solver.solve(inventory, impactTable);
		}
	}

	public AnalysisResult analyse(CalculationSetup setup) {
		log.trace("analyse product system - build inventory");
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		log.trace("analyse inventory");
		InventorySolver solver = new InventorySolver();
		if (setup.getImpactMethod() == null)
			return solver.analyse(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return solver.analyse(inventory, impactTable);
		}
	}

}
