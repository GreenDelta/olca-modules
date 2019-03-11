package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.Simulator;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.SimpleResult;

/**
 * TODO: this is only a simple test for testing the calculation of product
 * systems in other product systems -> remove this later
 */
public class SysInSys {

	public static void main(String[] args) {
		String dbPath = "C:/Users/Besitzer/openLCA-data-1.4/databases/zabtest";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"b53db562-1584-4c4c-bc6f-989f77348c8d");

		MatrixCache mcache = MatrixCache.createEager(db);
		IMatrixSolver solver = new JavaSolver();
		CalculationSetup setup = new CalculationSetup(
				CalculationType.MONTE_CARLO_SIMULATION, system);

		Simulator sim = Simulator.create(setup, mcache, solver);
		sim.getTechIndex().each((i, pp) -> {
			sim.pinnedProducts.add(pp);
		});

		SimpleResult r = sim.nextRun();

		FlowDescriptor flow = r.flowIndex.at(0);
		System.out.println(flow.name + "  -> " +
				r.getTotalFlowResult(flow));

	}

}
