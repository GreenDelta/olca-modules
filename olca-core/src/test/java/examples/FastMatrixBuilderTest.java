package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.CalculationType;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.FastMatrixBuilder;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class FastMatrixBuilderTest {

	public static void main(String[] args) {
		System.out.println("load ps");
		String workspace = "C:/Users/ms/openLCA-data-1.4";
		String dbPath = workspace
				+ "/databases/zimple";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"8a42a5d5-7244-4692-a735-067eeedbc710");
		CalculationSetup setup = new CalculationSetup(
				CalculationType.CONTRIBUTION_ANALYSIS, system);

		System.out.println("build it");
		long start = System.currentTimeMillis();

		FastMatrixBuilder builder = new FastMatrixBuilder(db, setup);
		MatrixData data = builder.build();

		long end = System.currentTimeMillis();
		double time = (end - start) / 1000.0;
		System.out.println("matrix build took " + time + " secs");

		// now the full result calculation
		start = System.currentTimeMillis();
		String juliaLibPath = "C:\\Users\\ms\\Projects\\openLCA\\eclipse";
		Julia.loadFromDir(new File(juliaLibPath));
		JuliaSolver solver = new JuliaSolver();
		LcaCalculator calc = new LcaCalculator(solver, data);
		FullResult r = calc.calculateFull();
		end = System.currentTimeMillis();
		time = (end - start) / 1000.0;
		System.out.println("calculation took " + time + " secs");

		System.out.println("done; flow count = " + r.flowIndex.size());
		System.out.println(r.totalFlowResults[0]);

	}

}
