package org.openlca.core.math;

import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.MatrixData;
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
 * A `Simulator` runs Monte-Carlo simulations with a given calculation setup.
 */
public class Simulator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ImpactMethodDescriptor impactMethod;
	private MatrixCache cache;
	private final IMatrixSolver solver;

	private SimulationResult result;
	private Inventory inventory;
	private ParameterTable parameterTable;
	private ImpactTable impactTable;
	private MatrixData data;
	private CalculationSetup setup;

	private boolean withContributions = false;

	/**
	 * Creates a new `Simulator` instance for the given setup.
	 */
	public Simulator(
			CalculationSetup setup,
			MatrixCache cache,
			IMatrixSolver solver) {
		this.impactMethod = setup.impactMethod;
		this.cache = cache;
		this.setup = setup;
		this.solver = solver;
	}

	/**
	 * Get the result of the simulation.
	 */
	public SimulationResult getResult() {
		return result;
	}

	/**
	 * When set to `true`, the result of each simulation will be a
	 * `ContributionResult`.
	 */
	public void withContributions(boolean b) {
		withContributions = b;
	}

	/**
	 * Generates random numbers and calculates the product system. Returns the
	 * simulation result if the calculation in this run finished without errors,
	 * otherwise `null` is returned (e.g. when the resulting matrix was
	 * singular). The returned result is appended to the result of the simulator
	 * (which you get via `getResult()`, so it does not need to be cached.
	 * 
	 * When the flag `withContributions` is set to true, the returned result
	 * will be an instance of `ContributionResult`.
	 */
	public SimpleResult nextRun() {
		if (inventory == null || data == null)
			setUp();
		try {
			log.trace("next simulation run");
			FormulaInterpreter interpreter = parameterTable.simulate();
			inventory.simulate(data, interpreter);
			if (impactTable != null) {
				impactTable.simulate(data.impactMatrix, interpreter);
			}
			LcaCalculator calc = new LcaCalculator(solver, data);
			SimpleResult result = withContributions
					? calc.calculateContributions()
					: calc.calculateSimple();
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
		parameterTable = ParameterTable.forSimulation(cache.getDatabase(),
				DataStructures.parameterContexts(setup, inventory.techIndex),
				setup.parameterRedefs);
		data = inventory.createMatrix(solver);
		result = new SimulationResult();
		result.techIndex = inventory.techIndex;
		result.flowIndex = inventory.flowIndex;
		if (impactMethod != null) {
			ImpactTable impactTable = ImpactTable.build(cache,
					impactMethod.getId(), inventory.flowIndex);
			if (impactTable.isEmpty()) {
				return;
			}
			this.impactTable = impactTable;
			data.impactMatrix = impactTable.createMatrix(solver);
			data.impactIndex = impactTable.impactIndex;
			result.impactIndex = impactTable.impactIndex;
		}
	}
}
