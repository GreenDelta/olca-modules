package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.SimulationGraph;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.SimpleResult;

public class SimulationGraphExample {

	public static void main(String[] args) {
		String workspace = "C:/Users/ms/openLCA-data-1.4";
		String dbPath = workspace
				+ "/databases/zabtest";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"b53db562-1584-4c4c-bc6f-989f77348c8d");
		CalculationSetup setup = new CalculationSetup(
				CalculationType.MONTE_CARLO_SIMULATION, system);

		IMatrixSolver solver = new JavaSolver();
		SimulationGraph g = SimulationGraph.build(
				MatrixCache.createLazy(db), setup, solver);

		double min = 0;
		double max = 0;
		int i = 10;
		for (int k = 0; k < 1_000_000; k++) {
			SimpleResult r = g.nextRun();
			double val = r.totalFlowResults[0];
			if (k == 0) {
				min = val;
				max = val;
			} else {
				min = Math.min(min, val);
				max = Math.max(max, val);
			}
			if ((k + 1) % i == 0) {
				System.out.println(
						"after " + i + " iterations: min=" + min + " max="
								+ max);
				i *= 10;
			}
		}
	}

}
