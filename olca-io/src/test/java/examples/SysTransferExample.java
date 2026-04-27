package examples;

import org.openlca.core.DataDir;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.olca.systransfer.ProviderIndex;
import org.openlca.io.olca.systransfer.ProviderInfo;

public class SysTransferExample {

	public static void main(String[] args) {

		try (var db = DataDir.get().openDatabase("ei312-apos")) {
			var start = System.nanoTime();
			var providers = ProviderIndex.of(db);
			var duration = (System.nanoTime() - start) / 1e9;
			System.out.println("Found all  provider candidates in "
				+ duration + " seconds.");

			var system = db.getDescriptors(ProductSystem.class)
				.stream()
				.findFirst()
				.map(d -> db.get(ProductSystem.class, d.id))
				.orElseThrow();
			start = System.nanoTime();
			var systemProviders = ProviderIndex.of(db, system);
			duration = (System.nanoTime() - start) / 1e9;
			System.out.println("Found  provider candidates for system "
				+ system.name + " in " + duration + " seconds.");

		}

	}
}
