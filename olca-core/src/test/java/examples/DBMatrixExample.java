package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.results.FullResult;
import org.openlca.nativelib.NativeLib;

/**
 * Shows how you build the matrices of the complete database for the direct
 * calculation.
 */
public class DBMatrixExample {

	public static void main(String[] args) {
		NativeLib.loadFrom(DataDir.root());
		try (var db = Derby.fromDataDir("ei2")) {
			System.out.println("build it");
			long start = System.currentTimeMillis();
			var techIndex = TechIndex.of(db);
			var matrices = MatrixData.of(db, techIndex).build();
			long end = System.currentTimeMillis();
			System.out.printf(
				"matrix build took %.3f seconds%n",
				(end - start) / 1000.0);

			// now the full result calculation
			start = System.currentTimeMillis();
			var r = FullResult.of(db, matrices);
			end = System.currentTimeMillis();
			System.out.printf(
				"calculation took %.3f seconds",
				(end - start) / 1000.0);

			System.out.println("done; flow count = " + r.enviIndex().size());
			System.out.println(r.totalFlowResults()[0]);

		}

	}

}
