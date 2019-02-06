package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.cache.MatrixCache;
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
		String dbPath = "C:/Users/Besitzer/openLCA-data-1.4/databases/sysinsys";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"109b6065-874d-463f-a87a-f4302d902b7f");
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, system);
		SystemCalculator calc = new SystemCalculator(
				MatrixCache.createEager(db), new JavaSolver());

		FullResult r = calc.calculateFull(setup);
		FlowDescriptor flow = r.flowIndex.at(0);
		System.out.println(
				flow.name + "  -> " +
						r.getTotalFlowResult(flow));

		System.out.println("Works");
	}

}
