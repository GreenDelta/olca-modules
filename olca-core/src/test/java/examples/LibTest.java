package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class LibTest {

	public static void main(String[] args) throws Exception {
		Julia.load();
		var workspace = "C:/Users/Win10/openLCA-data-1.4";
		var db = new DerbyDatabase(new File(
				workspace + "/databases/methods"));
		var libDir = new LibraryDir(new File(
				workspace + "/libraries"));


		// var sysID = "12398baf-cb24-4017-a9db-47761f9688c6";
		// var system = db.get(ProductSystem.class, sysID);
		var process = db.get(Process.class,
				"e78c028d-dc1d-39d3-838e-955a60f05c9e");
		var system = ProductSystem.of(process);
		system.withoutNetwork = true;

		var setup = new CalculationSetup(system);
		setup.impactMethod = Descriptor.of(
				db.get(ImpactMethod.class, "effb055a-ad78-39bd-8dc0-341411db4ae7"));

		var start =  System.currentTimeMillis();
		var result = new SystemCalculator(db, new JuliaSolver())
				.withLibraries(libDir)
				.calculateFull(setup);
		var end = System.currentTimeMillis();
		System.out.printf("Calculation done in %d ms %n", (end - start));

		result.impactIndex.each((i, impact) -> {
			System.out.printf("%s : %.4f\n",
					impact.name, + result.getTotalImpactResult(impact));
		});

		db.close();
	}

}
