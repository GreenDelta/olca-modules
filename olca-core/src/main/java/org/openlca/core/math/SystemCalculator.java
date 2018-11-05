package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.CostVector;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
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
		return calculator(setup).calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		return calculator(setup).calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		return calculator(setup).calculateFull();
	}

	private LcaCalculator calculator(CalculationSetup setup) {
		IDatabase db = matrixCache.getDatabase();
		Inventory inventory = DataStructures.createInventory(setup,
				matrixCache);
		ParameterTable parameters = DataStructures.createParameterTable(
				db, setup, inventory);
		FormulaInterpreter interpreter = parameters.createInterpreter();
		MatrixData data = inventory.createMatrix(solver, interpreter);
		if (setup.impactMethod != null) {
			ImpactTable impacts = ImpactTable.build(matrixCache,
					setup.impactMethod.getId(), inventory.flowIndex);
			data.impactMatrix = impacts.createMatrix(solver, interpreter);
			data.impactIndex = impacts.categoryIndex;
		}
		if (setup.withCosts) {
			data.costVector = CostVector.build(inventory, db);
		}
		return new LcaCalculator(solver, data);
	}
}
