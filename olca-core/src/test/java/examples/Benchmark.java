package examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.DenseSolver;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.eigen.NativeLibrary;

public class Benchmark {

	public static void main(String[] args) {
		if (NativeLibrary.isLoaded())
			return;
		String tempDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tempDirPath);
		NativeLibrary.loadFromDir(tmpDir);

		IMatrixSolver solver = new DenseSolver();

		int runs = 1;
		// IDatabase db = new
		// MySQLDatabase("jdbc:mysql://localhost:3306/openlca",
		// "root", "");
		IDatabase db = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/openlca_ei3_pre", "root", "");
		MatrixCache cache = MatrixCache.createEager(db);
		ProductSystem system = new ProductSystemDao(db).getForId(654886);
		Inventory inventory = DataStructures.inventory(
				system,
				AllocationMethod.USE_DEFAULT,
				cache,
				Collections.emptyMap());
		MatrixData data = inventory.createMatrix(solver);
		LcaCalculator calculator = new LcaCalculator(solver, data);

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
		for (FlowDescriptor flow : result.getFlows()) {
			System.out.printf("%s \t %.10f \n", flow.name,
					result.getTotalFlowResult(flow));
		}
	}
}
