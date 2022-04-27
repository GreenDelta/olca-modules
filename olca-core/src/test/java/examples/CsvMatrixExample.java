package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.MatrixExport;
import org.openlca.core.model.ProductSystem;

/*
 * This example shows how to transform a product system into a set of matrices,
 * and export these matrices and their indices into a platform independent CSV
 * format so that they can be further processed with other tools.
 */
public class CsvMatrixExample {

	public static void main(String[] args) {

		try (var db = DataDir.get().openDatabase("ei2")) {

			// build and calculate the system
			var system = db.get(ProductSystem.class,
				"7d1cbce0-b5b3-47ba-95b5-014ab3c7f569");
			var techIndex = TechIndex.of(db, system);
			var data = MatrixData.of(db, techIndex)
				.withUncertainties(true)
				.withImpacts(ImpactIndex.of(db))
				.build();

			// create and export the matrix data
			var exportDir = new File("target/data");
			MatrixExport.toCsv(db, exportDir, data)
				.writeAll();
		}
	}
}
