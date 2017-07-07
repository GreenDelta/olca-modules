package org.openlca.core.matrix.solvers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;

public class Benchmark {

	public static void main(String[] args) {
		TestSession.loadLib();

		IMatrixSolver solver = new DenseSolver();

		int runs = 1;
		// IDatabase db = new
		// MySQLDatabase("jdbc:mysql://localhost:3306/openlca",
		// "root", "");
		IDatabase db = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/openlca_ei3_pre", "root", "");
		MatrixCache cache = MatrixCache.createEager(db);
		ProductSystem system = db.createDao(ProductSystem.class).getForId(
				654886);
		Inventory inventory = DataStructures.createInventory(system, cache);
		InventoryMatrix matrix = inventory.createMatrix(solver);
		LcaCalculator calculator = new LcaCalculator(solver, matrix);

		System.out.println("Inventory ready. Type enter to start!");
		try {
			InputStreamReader r = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(r);
			reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Run new benchmark");
		System.out.println("run \t t(quick)[ms] \t t(analyse)[ms] \t mem[MB]");
		FullResult result = null;
		for (int run = 1; run <= runs; run++) {
			result = null;
			System.gc();
			long start = System.currentTimeMillis();
			calculator.calculateSimple();
			long quick = System.currentTimeMillis() - start;
			System.gc();
			start = System.currentTimeMillis();
			result = calculator.calculateFull();
			long analysis = System.currentTimeMillis() - start;
			Runtime r = Runtime.getRuntime();
			double mem = (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
			System.out.printf("%d \t %d \t %d \t %.2f \n", run, quick,
					analysis, mem);
		}

		System.out.println("done");
		System.out.println("\nResults:\n");
		System.out.println("flowId \t result");
		for (long flowId : result.flowIndex.getFlowIds()) {
			System.out.printf("%d \t %.10f \n", flowId,
					result.getTotalFlowResult(flowId));
		}
	}
}
