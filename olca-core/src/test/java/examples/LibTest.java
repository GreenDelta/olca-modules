package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.solutions.LibrarySolutionProvider;

public class LibTest {

	public static void main(String[] args) throws Exception {
		var workspace = "C:/Users/ms/openLCA-data-1.4";
		var db = new DerbyDatabase(new File(
				workspace + "/databases/libtest"));
		var libDir = new LibraryDir(new File(
				workspace + "/libraries"));

		var sysID = "1e0d3aea-1640-4043-a44f-a5344f4427ec";
		var system = db.get(ProductSystem.class, sysID);
		var data = DataStructures.matrixData(
				db, new CalculationSetup(system));
		var provider = LibrarySolutionProvider.of(
				db,
				libDir,
				new JavaSolver(),
				data);


		var flowIdx = provider.flowIndex();

		var start =  System.currentTimeMillis();
		var result = provider.totalFlowResults();
		var end = System.currentTimeMillis();
		System.out.printf("Calculation done in %d ms %n", (end - start));

		for (int i = 0; i < result.length; i++) {
			var flow = flowIdx.at(i).flow;
			System.out.println(flow.name + "\t" + result[i]);
		}
		db.close();
	}

}
