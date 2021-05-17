package examples;

import org.openlca.core.database.Derby;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.ProductSystem;
import org.openlca.julia.Julia;

public class CalculationExample {

	public static void main(String[] args) {
		Julia.load();
		try (var db = Derby.fromDataDir("ei22")) {
			var system = db.get(ProductSystem.class,
				"7d1cbce0-b5b3-47ba-95b5-014ab3c7f569");
			var method = new ImpactMethodDao(db)
				.getDescriptorForRefId("207ffac9-aaa8-401d-ac90-874defd3751a");
			var setup = new CalculationSetup(system);
			setup.impactMethod = method;
			var calc = new SystemCalculator(db);
			var r = calc.calculateFull(setup);
			var f = r.enviIndex().at(0);
			System.out.println(f.flow().name + "  -> " + r.getTotalFlowResult(f));
			var impact =  r.impactIndex().at(0);
			System.out.println(impact.name + "  -> " + r.getTotalImpactResult(impact));
		}
	}
}
