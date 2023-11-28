package examples;

import org.openlca.core.DataDir;
import org.openlca.core.model.ProductSystem;
import org.openlca.util.ProviderChainRemoval;

import java.util.concurrent.ThreadLocalRandom;

public class ProviderChainRemovalBenchmark {

	public static void main(String[] args) {
		var dbName = "ecoinvent_391_apos_upr_n3_20230629";
		var sysId = "3488ecfe-3839-452f-8df2-32e444a7fef8";
		var rand = ThreadLocalRandom.current();
		try (var db = DataDir.get().openDatabase(dbName)) {
			System.out.println(
					"| Run | Link   | Time [ms] | Removed processes |");
			System.out.println(
					"|-----|--------|-----------|-------------------|"
			);
			var system = db.get(ProductSystem.class, sysId);
			for (int i = 1; i < 11; i++) {
				var sys = system.copy();
				var k = rand.nextInt(0, sys.processLinks.size());
				var link = sys.processLinks.get(k);
				var start = System.nanoTime();
				var n = ProviderChainRemoval.on(sys).remove(link);
				var time = System.nanoTime() - start;
				var ms = ((double) time) / 1e6;
				System.out.printf("| %3d | %6d | %9.2f | %17d |%n",
						i, k, ms, n);
			}
		}
	}
}
