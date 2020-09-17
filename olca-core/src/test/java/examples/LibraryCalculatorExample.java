package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProductSystem;

public class LibraryCalculatorExample {

	public static void main(String[] args) throws Exception {

		var workspace = "C:/Users/Win10/openLCA-data-1.4";
		var db = new DerbyDatabase(new File(
				workspace + "/databases/libre2"));
		var libDir = new LibraryDir(new File(
				workspace + "/libraries"));

		var system = db.get(ProductSystem.class,
				"6b32cda2-5aa4-44b9-b32a-c654da48436d");
		var setup = new CalculationSetup(system);

		var result = new SystemCalculator(db, new JavaSolver())
				.withLibraries(libDir)
				.calculateSimple(setup);

		var flow = result.flowIndex.at(0);
		System.out.println(flow.flow.name
				+ " => " + result.getTotalFlowResult(flow));

		db.close();

	}

}
