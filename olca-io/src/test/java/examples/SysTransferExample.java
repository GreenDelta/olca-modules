package examples;

import org.openlca.core.DataDir;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.olca.systransfer.MatchingStrategy;
import org.openlca.io.olca.systransfer.TransferConfig;
import org.openlca.io.olca.systransfer.TransferPlan;

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

			var config = new TransferConfig(
				source, target, system, MatchingStrategy.values());
			var plan = TransferPlan.createFrom(config).orElseThrow();
		}

	}

}
