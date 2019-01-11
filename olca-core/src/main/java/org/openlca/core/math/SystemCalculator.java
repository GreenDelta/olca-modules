package org.openlca.core.math;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: this class only dispatches the method calls to the
// LcaCalculator class and thus may be removed
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
		MatrixData data = DataStructures.matrixData(
				setup, solver, matrixCache);
		LcaCalculator calc = new LcaCalculator(solver, data);
		return calc.calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		MatrixData data = DataStructures.matrixData(
				setup, solver, matrixCache);
		LcaCalculator calc = new LcaCalculator(solver, data);
		return calc.calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		MatrixData data = DataStructures.matrixData(
				setup, solver, matrixCache);
		LcaCalculator calc = new LcaCalculator(solver, data);
		return calc.calculateFull();
	}
}
