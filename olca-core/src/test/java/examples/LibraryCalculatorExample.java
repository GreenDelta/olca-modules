package examples;

import java.io.File;
import java.util.Collections;

import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.library.LibraryCalculator;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.matrix.solvers.JavaSolver;

public class LibraryCalculatorExample {


	public static void main(String[] args) throws Exception {

		var workspace = "C:/Users/Win10/openLCA-data-1.4";
		var db = new DerbyDatabase(new File(
				workspace + "/databases/libtest_ei22_gen"));
		var libDir = new LibraryDir(new File(
				workspace + "/libraries"));

		var system = new ProductSystemDao(db).getForRefId(
				"6e0fcd0c-9c2b-4c7b-a432-3a26af371eb2");
		var setup = new CalculationSetup(system);
		var foregroundData = DataStructures.matrixData(
				setup, db, Collections.emptyMap());

		var calculator = new LibraryCalculator(
				db, libDir, new JavaSolver());
		var result = calculator.calculate(foregroundData);

		var flow = result.flowIndex.at(0);
		System.out.println(flow.flow.name
				+ " => " + result.getTotalFlowResult(flow));

		db.close();

	}

}
