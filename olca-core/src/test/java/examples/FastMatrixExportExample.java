package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FastMatrixBuilder;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.io.Csv;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;

/*
 * This example uses the `FastMatrixBuilder` to export matrices with LCI and LCIA
 * from a database.
 */
public class FastMatrixExportExample {

	public static void main(String[] args) {

		// set the path to the database folder here
		String dbDir = "C:/Users/ms/openLCA-data-1.4/databases/ecoinvent_2_2_unit";
		try (IDatabase db = new DerbyDatabase(new File(dbDir))) {

			// The FastMatrixBuilder takes a normal calculation setup
			// with a product system but it will build the LCI matrices
			// for all processes in the database.
			ProductSystem system = new ProductSystemDao(db).getForRefId(
					"53f9b9db-139f-4617-bf2b-8fc715b3cd16");
			CalculationSetup setup = new CalculationSetup(system);
			setup.allocationMethod = AllocationMethod.USE_DEFAULT;

			// the LCIA method for which we export the characterization
			// factors of the indicators
			setup.impactMethod = new ImpactMethodDao(db).getDescriptorForRefId(
					"207ffac9-aaa8-401d-ac90-874defd3751a");

			// set withCosts to true, if you also want to export LCC matrices
			// setup.withCosts = true;

			// define the folder where the matrices should be written
			// the export will create the following files:
			// A.csv: the product*product technology matrix
			// B.csv: the flow*product intervention matrix
			// C.csv: the indicator*flow matrix with characterization factors
			// indexA.csv: the index of the matrix A
			// indexB.csv: the index of the matrix B
			// indexC.csv: the index of the matrix C
			File outFolder = new File("target/matrices");
			FastMatrixBuilder builder = new FastMatrixBuilder(db, setup);
			MatrixData data = builder.build();
			outFolder.mkdirs();
			Csv.write(data, db, outFolder);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
