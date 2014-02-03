package org.openlca.core.math;

import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
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
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		LcaCalculator calculator = new LcaCalculator(solver);
		if (setup.getImpactMethod() == null)
			return calculator.calculateSimple(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return calculator.calculateSimple(inventory, impactTable);
		}
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		LcaCalculator calculator = new LcaCalculator(solver);
		if (setup.getImpactMethod() == null)
			return calculator.calculateContributions(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return calculator.calculateContributions(inventory, impactTable);
		}
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		Inventory inventory = Calculators.createInventory(setup, matrixCache);
		LcaCalculator calculator = new LcaCalculator(solver);
		if (setup.getImpactMethod() == null)
			return calculator.calculateFull(inventory);
		else {
			ImpactTable impactTable = Calculators.createImpactTable(
					setup.getImpactMethod(), inventory.getFlowIndex(),
					matrixCache);
			return calculator.calculateFull(inventory, impactTable);
		}
	}
}
