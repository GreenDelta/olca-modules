package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.DataStructures;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.Sankey;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class SankeyExample {
	public static void main(String[] args) throws Exception {
		var dbDir = "C:/Users/ms/openLCA-data-1.4/databases/ecoinvent_apos_37_up_20200908_1_";
		var db = new DerbyDatabase(new File(dbDir));
		var system = db.get(
				ProductSystem.class,
				"561e8272-5322-4971-9dbf-d791d3f006f9");
		var setup = new CalculationSetup(system);


		var libDir = "C:/Users/ms/Projects/openLCA/repos/olca-rust/bin";
		Julia.loadFromDir(new File(libDir));
		var solver = new JuliaSolver();
		var calc = new SystemCalculator(db, solver);

		var start = System.currentTimeMillis();
		var result = calc.calculateFull(setup);
		var end = System.currentTimeMillis();
		System.out.println("Computed result in: "
				+ ((double) (end - start) / 1000d));

		IndexFlow flow = result.flowIndex.at(242);

		start = System.currentTimeMillis();
		var sankey = Sankey.of(flow, result)
				.withMaximumNodeCount(50)
				.withMinimumShare(0.01)
				.build();
		end = System.currentTimeMillis();
		System.out.println("Computed sankey in: "
				+ ((double) (end - start) / 1000d));

		System.out.println(sankey.toDot());

		db.close();
	}

	private static MatrixData measureCompression(
			DerbyDatabase db,
			CalculationSetup setup) {

		var start = System.currentTimeMillis();
		var data = DataStructures.matrixData(db, setup);
		var end = System.currentTimeMillis();

		System.out.println("Loading the matrices took: "
				+ ((double) (end - start) / 1000d));

		start = System.currentTimeMillis();
		data.compress();
		end = System.currentTimeMillis();
		System.out.println("Compressing the matrices took: "
				+ ((double) (end - start) / 1000d));
		return data;
	}
}
