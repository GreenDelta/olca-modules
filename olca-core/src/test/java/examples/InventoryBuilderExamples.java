package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

// TODO: just an example how to use the new inventory builder -> delete this
// when we are ready
public class InventoryBuilderExamples {

	public static void main(String[] args) {

		String dbPath = "C:/Users/ms/openLCA-data-1.4/databases/e_3_3_er_database_es2050_v1_7_1";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db)
				.getForRefId("9aae83bf-e300-49c5-b62b-981546bcf8d6");

		CalculationSetup setup = new CalculationSetup(system);
		setup.impactMethod = new ImpactMethodDao(db).getDescriptorForRefId(
				"44f7066c-33fd-49d2-86ec-2b94677bf6d0");
		Julia.loadFromDir(new File("./olca-core/julia/libs"));
		MatrixSolver solver = new JuliaSolver();
		SystemCalculator calc = new SystemCalculator(db, solver);
		ContributionResult r = calc.calculateContributions(setup);
		for (ImpactDescriptor impact : r.getImpacts()) {
			System.out.println(impact.refId + "\t" +
					impact.name + "\t" + r.getTotalImpactResult(impact));
		}
	}
}
