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
	private final IMatrixFactory factory;
	private final ISolver solver;

	public SystemCalculator(MatrixCache cache, IMatrixFactory factory) {
		this(cache, factory, factory.getDefaultSolver());
	}

	public SystemCalculator(MatrixCache cache, IMatrixFactory factory,
			ISolver solver) {
		this.matrixCache = cache;
		this.factory = factory;
		this.solver = solver;
	}

	public InventoryResult solve(CalculationSetup setup) {
		log.trace("solve product system - build inventory");
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		log.trace("solve inventory");
		InventoryCalculator calculator = new InventoryCalculator(factory,
				solver);
		if (setup.getImpactMethod() == null)
			return calculator.solve(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return calculator.solve(inventory, impactTable);
		}
	}

	public AnalysisResult analyse(CalculationSetup setup) {
		log.trace("analyse product system - build inventory");
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		log.trace("analyse inventory");
		InventoryCalculator calculator = new InventoryCalculator(factory,
				solver);
		if (setup.getImpactMethod() == null)
			return calculator.analyse(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return calculator.analyse(inventory, impactTable);
		}
	}

}
