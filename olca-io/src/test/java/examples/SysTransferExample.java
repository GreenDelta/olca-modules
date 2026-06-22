package examples;

import org.openlca.core.DataDir;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.olca.migration.MatchingStrategy;
import org.openlca.io.olca.migration.MigrationConfig;
import org.openlca.io.olca.migration.MigrationPlan;

public class SysTransferExample {

	public static void main(String[] args) {

		var dir = DataDir.get();
		try (var source = dir.openDatabase("SiToLub");
		     var target = dir.openDatabase("ei321-apos")) {
			var system = source.getDescriptors(ProductSystem.class)
				.stream()
				.map(d -> source.get(ProductSystem.class, d.id))
				.findFirst()
				.orElseThrow();

			var config = new MigrationConfig(
				source, target, system, MatchingStrategy.values());
			var plan = MigrationPlan.createFrom(config).orElseThrow();
		}

	}

}
