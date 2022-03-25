package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.nativelib.NativeLib;

// TODO: just an example how to use the new inventory builder -> delete this
// when we are ready
public class InventoryBuilderExamples {

	public static void main(String[] args) {
		NativeLib.loadFrom(DataDir.root());

		try (var db = Derby.fromDataDir("e_3_3_er_database_es2050_v1_7_1")) {
			var system = db.get(ProductSystem.class,
				"9aae83bf-e300-49c5-b62b-981546bcf8d6");
			var method = db.get(ImpactMethod.class,
				"44f7066c-33fd-49d2-86ec-2b94677bf6d0");
			var setup = CalculationSetup.contributions(system)
				.withImpactMethod(method);
			var calc = new SystemCalculator(db);
			var r = calc.calculateContributions(setup);
			for (var impact : r.getImpacts()) {
				System.out.println(impact.refId + "\t" +
					impact.name + "\t" + r.getTotalImpactResult(impact));
			}
		}
	}
}
