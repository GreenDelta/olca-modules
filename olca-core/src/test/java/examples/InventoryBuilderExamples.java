package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.InventoryBuilder2;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
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

		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, system);
		TechIndex idx = DataStructures.createProductIndex(system, db);
		InventoryConfig conf = new InventoryConfig(db, idx);
		conf.interpreter = DataStructures.interpreter(db, setup, idx);
		InventoryBuilder2 builder = new InventoryBuilder2(conf);
		MatrixData data = builder.build();

		Julia.loadFromDir(new File("./olca-core/julia/libs"));
		IMatrixSolver solver = new JuliaSolver();

		LcaCalculator calc = new LcaCalculator(solver, data);
		ContributionResult r = calc.calculateContributions();

		for (FlowDescriptor flow : r.getFlows()) {
			System.out.println(flow.refId + "\t" +
					flow.name + "\t" + r.getTotalFlowResult(flow));
		}
	}
}
