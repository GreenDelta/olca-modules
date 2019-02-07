package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.FullResult;

/**
 * TODO: this is only a simple test for testing the calculation of product
 * systems in other product systems -> remove this later
 */
public class SysInSys {

	public static void main(String[] args) {
		String dbPath = "C:/Users/ms/openLCA-data-1.4/databases/sysinsys";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getAll().get(1);

		MatrixCache mcache = MatrixCache.createEager(db);
		IMatrixSolver solver = new JavaSolver();
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, system);

		SystemCalculator calc = new SystemCalculator(mcache, solver);
		FullResult r = calc.calculateFull(setup);
		FlowDescriptor flow = r.flowIndex.at(0);
		System.out.println(flow.name + "  -> " +
				r.getTotalFlowResult(flow));

		System.out.println(r.techMatrix);

		r.techIndex.each((i, p) -> {
			System.out.println(i + " -> " + p.process.name);
		});
	}

}
