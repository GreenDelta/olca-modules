package examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

import org.openlca.core.database.Derby;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.julia.Julia;

public class Benchmark {

	public static void main(String[] args) {
		Julia.load();
		var db = Derby.fromDataDir("ei37-apos");

		var processes = db.allDescriptorsOf(Process.class);
		var i = new Random().nextInt(processes.size());
		var process = db.get(Process.class, processes.get(i).id);
		var system = ProductSystem.of(process);
		var setup = CalculationSetup.simple(system);

		var data = MatrixData.of(db, setup);

		System.out.println("Inventory ready. Type enter to start!");
		try (var r = new InputStreamReader(System.in);
				 var reader = new BufferedReader(r)) {
			reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int runs = 1;
		System.out.println("Run new benchmark");
		System.out.println("run \t t(quick)[ms] \t t(analyse)[ms] \t mem[MB]");
		SimpleResult result = null;
		for (int run = 1; run <= runs; run++) {
			System.gc();
			long start = System.currentTimeMillis();
			result = SimpleResult.of(db, data);
			long quick = System.currentTimeMillis() - start;
			System.gc();
			start = System.currentTimeMillis();
			FullResult.of(db, data);
			long analysis = System.currentTimeMillis() - start;
			var r = Runtime.getRuntime();
			double mem = (r.totalMemory() - r.freeMemory()) / (1024.0 * 1024.0);
			System.out.printf("%d \t %d \t %d \t %.2f \n", run, quick,
				analysis, mem);
		}

		System.out.println("done");
		System.out.println("\nResults:\n");
		System.out.println("flowId \t result");
		for (EnviFlow f : result.getFlows()) {
			System.out.printf("%s \t %.10f \n", f.flow().name,
				result.getTotalFlowResult(f));
		}
	}
}
