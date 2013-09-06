package org.openlca.shell;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.SimulationResult;
import org.openlca.io.xls.results.SimulationResultExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	private long systemId;
	private int runs;
	private IDatabase database;
	private File file;

	public void exec(Shell shell, String[] args) {
		boolean valid = parseArgs(shell, args);
		if (!valid)
			return;
		try {
			log.trace("run Monte-Carlo-Simulation, system = {}, {} runs",
					systemId, runs);
			ProductSystem system = database.createDao(ProductSystem.class)
					.getForId(systemId);
			CalculationSetup setup = new CalculationSetup(system,
					CalculationSetup.MONTE_CARLO_SIMULATION);
			setup.setAllocationMethod(AllocationMethod.USE_DEFAULT);
			Simulator simulator = new Simulator(setup, database);
			for (int i = 0; i < runs; i++) {
				log.trace("next run {} started", i + 1);
				boolean success = simulator.nextRun();
				log.trace("run {} finished, success = {}", i + 1, success);
			}
			exportResults(simulator);
			log.trace("all done");
		} catch (Exception e) {
			log.error("failed to run simulation", e);
		}
	}

	private void exportResults(Simulator simulator) throws Exception {
		if (file != null) {
			log.trace("export results");
			SimulationResult result = simulator.getResult();
			EntityCache cache = EntityCache.create(database);
			SimulationResultExport export = new SimulationResultExport(result,
					cache);
			export.run(file);
		}
	}

	private boolean parseArgs(Shell shell, String[] args) {
		if (shell.getDatabase() == null) {
			log.error("no database connection");
			return false;
		}
		this.database = shell.getDatabase();
		if (args.length < 2) {
			log.error("a process ID and a number of simulation runs is expected");
			return false;
		}
		try {
			systemId = Long.parseLong(args[0]);
			runs = Integer.parseInt(args[1]);
		} catch (Exception e) {
			log.error(
					"the product system id ot the number of runs is not valid",
					e);
			return false;
		}

		if (args.length > 2 && args[2] != null)
			file = new File(args[2]);
		else
			log.warn("you did not specified an output file for the results");
		return true;
	}

}
