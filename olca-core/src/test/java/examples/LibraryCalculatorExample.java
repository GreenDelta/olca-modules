package examples;

import java.io.File;

import org.openlca.core.database.Derby;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;

public class LibraryCalculatorExample {

	public static void main(String[] args) throws Exception {

		var workspace = "C:/Users/Win10/openLCA-data-1.4";
		var db = new Derby(new File(workspace + "/databases/libre2"));

		var system = db.get(ProductSystem.class,
				"6b32cda2-5aa4-44b9-b32a-c654da48436d");
		var setup = CalculationSetup.simple(system);

		var result = new SystemCalculator(db).calculate(setup);

		var flow = result.enviIndex().at(0);
		System.out.println(flow.flow().name
				+ " => " + result.getTotalFlowResult(flow));

		db.close();

	}

}
