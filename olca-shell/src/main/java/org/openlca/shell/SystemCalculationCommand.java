package org.openlca.shell;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.Cache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FlowResult;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.InventoryResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemCalculationCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void solve(Shell shell, String[] args) {
		boolean valid = checkArgs(shell, args);
		if (!valid)
			return;
		try {
			IDatabase db = shell.getDatabase();
			long systemId = Long.parseLong(args[0]);
			log.trace("load product system {}", systemId);
			ProductSystem system = db.createDao(ProductSystem.class).getForId(
					systemId);
			log.trace("solve system with {} processes", system.getProcesses()
					.size());
			SystemCalculator calculator = new SystemCalculator(db);
			InventoryResult result = calculator.solve(system);
			log.trace("print results");
			Cache cache = new Cache(db);
			printInventoryResult(result, cache);
		} catch (Exception e) {
			log.error("failed to solve system with ID " + args[0], e);
		}
	}

	private void printInventoryResult(InventoryResult result, Cache cache) {
		List<FlowResult> flowResults = InventoryResults.getFlowResults(result,
				cache);
		List<String[]> records = new ArrayList<>();
		for (FlowResult flowResult : flowResults) {
			String[] record = new String[2];
			record[0] = flowResult.getFlow().getName();
			record[1] = Double.toString(flowResult.getValue());
			records.add(record);
		}
		new TablePrinter(System.out).print(records);
	}

	private boolean checkArgs(Shell shell, String[] args) {
		if (args.length < 1) {
			log.error("a process ID is expected");
			return false;
		}
		if (shell.getDatabase() == null) {
			log.error("no database connection");
			return false;
		}
		return true;
	}
}
