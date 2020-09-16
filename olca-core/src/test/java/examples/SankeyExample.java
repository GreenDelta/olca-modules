package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.Sankey;

public class SankeyExample {
	public static void main(String[] args) throws Exception {

		var workspace = "C:/Users/ms/openLCA-data-1.4";
		var db = new DerbyDatabase(new File(
				workspace + "/databases/libtest"));
		var libDir = new LibraryDir(new File(
				workspace + "/libraries"));

		var sysID = "1e0d3aea-1640-4043-a44f-a5344f4427ec";
		var system = db.get(ProductSystem.class, sysID);
		var setup = new CalculationSetup(system);

		var data = DataStructures.matrixData(db, setup);
		var calc = new LcaCalculator(new JavaSolver(), data);

		var start = System.currentTimeMillis();
		var result = calc.calculateWithLibraries(db, libDir);
		var end = System.currentTimeMillis();
		System.out.println("Computed result in: "
				+ ((double) (end - start) / 1000d));

		IndexFlow flow = result.flowIndex.at(42);

		start = System.currentTimeMillis();
		var sankey = Sankey.of(flow, result)
				.withMaximumNodeCount(50)
				.withMinimumShare(0.01)
				.build();
		end = System.currentTimeMillis();
		System.out.println("Computed sankey in: "
				+ ((double) (end - start) / 1000d));

		System.out.println(sankey.toDot());

		db.close();
	}

}
