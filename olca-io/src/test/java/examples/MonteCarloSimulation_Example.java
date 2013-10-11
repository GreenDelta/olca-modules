package examples;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.Simulator;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.SimulationResult;
import org.openlca.io.xls.results.SimulationResultExport;
import org.openlca.jblas.Library;

/**
 * Shows how to run a Monte Carlo Simulation and export the result.
 */
public class MonteCarloSimulation_Example {

	public static void main(String[] args) {

		// load the high-performance BLAS library
		String blasLibPath = "C:/Users/Dell/Downloads";
		Library.loadFromDir(new File(blasLibPath));

		// connect to a database and initialize a matrix cache
		String dbPath = "C:/Users/Dell/openLCA-data-demo-1.4/databases/ecoinvent2";
		IDatabase database = new DerbyDatabase(new File(dbPath));
		MatrixCache cache = MatrixCache.createLazy(database);

		// load a product system
		ProductSystem system = database.createDao(ProductSystem.class)
				.getForId(172923);

		// create the calculation set-up
		int runs = 100;
		CalculationSetup setup = new CalculationSetup(system,
				CalculationSetup.MONTE_CARLO_SIMULATION);
		setup.setNumberOfRuns(runs);

		// run the simulation
		Simulator simulator = new Simulator(setup, cache);
		for (int i = 0; i < runs; i++)
			simulator.nextRun();

		// export the result
		String exportPath = "C:/Users/Dell/Downloads/172923_sim_results.xls";
		SimulationResult result = simulator.getResult();
		SimulationResultExport export = new SimulationResultExport(result,
				EntityCache.create(database));
		try {
			export.run(new File(exportPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
