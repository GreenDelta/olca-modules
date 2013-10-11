package org.openlca.shell;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.InventorySolver;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.ProductIndexBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
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
			MatrixCache cache = MatrixCache.createLazy(database);
			ProductIndexBuilder builder = new ProductIndexBuilder(cache);
			ProductIndex index = builder.build(refProduct, 1d);
			logTime("product index created");

			log.trace("Build inventory");
			InventoryBuilder matrixBuilder = new InventoryBuilder(cache);
			Inventory matrix = matrixBuilder.build(index,
					AllocationMethod.USE_DEFAULT);
			logTime("inventory matrix created");

			log.trace("Solve inventory");
			InventorySolver solver = new InventorySolver();
			InventoryResult result = solver.solve(matrix);
			logTime("inventory solved");

			log.trace("finished after {} msec", totalTime);

			System.out.println("\n\nResults:");
			System.out.println("flow \t result");
			FlowIndex flowIndex = matrix.getFlowIndex();
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
