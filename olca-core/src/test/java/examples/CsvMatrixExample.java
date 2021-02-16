package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.io.Csv;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.julia.JuliaSolver;

/*
 * This example shows how to transform a product system into a set of matrices,
 * calculate the full analysis result based on these matrices, and export all
 * matrices and their indices into a platform independent CSV format so that
 * they can be further processed with other tools.
 */
public class CsvMatrixExample {

	public static void main(String[] args) {
		String workspace = "C:/Users/Besitzer/openLCA-data-1.4";
		String dbPath = workspace
				+ "/databases/ecoinvent35_cut_off_up_20181210";
		IDatabase db = new DerbyDatabase(new File(dbPath));

		// create the calculation setup
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"2c46bdcc-9798-4e78-868c-020ff7b7fd74");
		CalculationSetup setup = new CalculationSetup(system);
		// setup.impactMethod = new ImpactMethodDao(db)
		// .getDescriptorForRefId(
		// "207ffac9-aaa8-401d-ac90-874defd3751a");

		// load the native and create the solver
		// TODO: load Julia libraries first here
		var solver = new JuliaSolver();

		// create and export the matrix data
		File exportDir = new File("target/data");
		MatrixData data = MatrixData.of(db, setup);
		Csv.write(data, db, exportDir);

		// calculate and export the result
		LcaCalculator calc = new LcaCalculator(
				solver, data);
		FullResult r = calc.calculateFull();
		Csv.write(r, db, exportDir);
	}

}
