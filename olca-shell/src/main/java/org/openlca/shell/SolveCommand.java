package org.openlca.shell;

import org.openlca.core.database.IDatabase;
import org.openlca.core.indices.ExchangeTable;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.indices.ProductIndexBuilder;
import org.openlca.core.math.InventorySolver;
import org.openlca.core.matrices.Inventory;
import org.openlca.core.matrices.InventoryBuilder;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.results.InventoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolveCommand {

	private long lastStop;
	private long totalTime;

	private Logger log = LoggerFactory.getLogger(getClass());

	public void exec(Shell shell, String[] args) {
		if (args.length < 1) {
			log.error("process product expected as argument");
			return;
		}
		IDatabase database = shell.getDatabase();
		if (database == null) {
			log.error("no database connection");
			return;
		}
		LongPair processProduct = fetchProduct(args);
		if (processProduct == null) {
			log.error("the process product has no valid format; "
					+ "it must be <numbler>#<number>");
			return;
		}
		run(processProduct, database);
	}

	private LongPair fetchProduct(String[] args) {
		String productString = args[0];
		if (productString == null || productString.isEmpty())
			return null;
		try {
			String[] parts = productString.split("#");
			return new LongPair(Long.parseLong(parts[0]),
					Long.parseLong(parts[1]));
		} catch (Exception e) {
			return null;
		}
	}

	private void run(LongPair refProduct, IDatabase database) {
		try {

			log.info("calculate product system {}#{}", refProduct.getFirst(),
					refProduct.getSecond());
			totalTime = 0;
			lastStop = System.currentTimeMillis();

			ProductIndexBuilder builder = new ProductIndexBuilder(database);
			ProductIndex index = builder.build(refProduct, 1d);
			logTime("product index created");

			log.trace("Build exchange table");
			ExchangeTable table = new ExchangeTable(database,
					index.getProcessIds());
			logTime("exchange table created");

			log.trace("Build flow index");
			FlowIndex flowIndex = new FlowIndex(index, table);
			logTime("flow index created");

			log.trace("Build inventory matrix");
			InventoryBuilder matrixBuilder = new InventoryBuilder(index,
					flowIndex);
			Inventory matrix = matrixBuilder.build(table,
					AllocationMethod.USE_DEFAULT);
			logTime("inventory matrix created");

			log.trace("Solve inventory");
			InventorySolver solver = new InventorySolver();
			InventoryResult result = solver.solve(matrix);
			logTime("inventory solved");

			log.trace("finished after {} msec", totalTime);

			System.out.println("\n\nResults:");
			System.out.println("flow \t result");
			for (int i = 0; i < flowIndex.size(); i++) {
				long flowId = flowIndex.getFlowAt(i);
				System.out.println(String.format("%s \t %s", flowId,
						result.getFlowResult(flowId)));
			}

		} catch (Exception e) {
			log.error("calculation failed", e);
		}
	}

	private void logTime(String message) {
		long stop = System.currentTimeMillis();
		long time = stop - lastStop;
		totalTime += time;
		lastStop = stop;
		long timeSec = time / 1000;
		long mem = Runtime.getRuntime().totalMemory() / 1024 / 1024;
		log.trace(String
				.format(message + "; time = %s sec (%s ms); memory = %s MB",
						timeSec, time, mem));

	}

}
