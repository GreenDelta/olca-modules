package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.solvers.DenseSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.eigen.NativeLibrary;

public class CalculationExample {

	public static void main(String[] args) {
		String dbPath = "C:/Users/ms/openLCA-data-1.4/databases/ecoinvent_2_2_unit";
		IDatabase db = new DerbyDatabase(new File(dbPath));

		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"7d1cbce0-b5b3-47ba-95b5-014ab3c7f569");
		ImpactMethodDescriptor method = new ImpactMethodDao(db)
				.getDescriptorForRefId("207ffac9-aaa8-401d-ac90-874defd3751a");
		CalculationSetup setup = new CalculationSetup(system);
		setup.impactMethod = method;

		NativeLibrary.loadFromDir(
				new File("C:/Users/ms/openLCA-data-1.4"));
		SystemCalculator calc = new SystemCalculator(db, new DenseSolver());
		FullResult r = calc.calculateFull(setup);
		IndexFlow f = r.flowIndex.at(0);
		System.out.println(f.flow.name + "  -> " + r.getTotalFlowResult(f));

		ImpactCategoryDescriptor impact = r.impactIndex.at(0);
		System.out.println(impact.name + "  -> " + r.getTotalImpactResult(impact));
	}

}
