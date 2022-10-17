package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.nativelib.NativeLib;

public class CalculationExample {

	public static void main(String[] args) {
		NativeLib.loadFrom(DataDir.get().root());
		try (var db = DataDir.get().openDatabase("ei22")) {
			var system = db.get(ProductSystem.class,
				"7d1cbce0-b5b3-47ba-95b5-014ab3c7f569");
			var method = new ImpactMethodDao(db)
				.getForRefId("207ffac9-aaa8-401d-ac90-874defd3751a");
			var setup = CalculationSetup.of(system)
				.withImpactMethod(method);
			var calc = new SystemCalculator(db);
			var r = calc.calculate(setup);
			var f = r.enviIndex().at(0);
			System.out.println(f.flow().name + "  -> " + r.getTotalFlowValueOf(f));
			var impact =  r.impactIndex().at(0);
			System.out.println(impact.name + "  -> " + r.totalImpactOf(impact));
		}
	}
}
