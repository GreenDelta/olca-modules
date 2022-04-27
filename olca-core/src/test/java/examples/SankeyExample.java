package examples;

import org.openlca.core.DataDir;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.Sankey;
import org.openlca.nativelib.NativeLib;

public class SankeyExample {
	public static void main(String[] args) {
		NativeLib.loadFrom(DataDir.get().root());
		var db = DataDir.get().openDatabase("ei37-apos");

		var system = db.get(
				ProductSystem.class,
				"2bc48e5d-7a6c-4655-8477-42d2e53fa171");

		var start = System.currentTimeMillis();
		var result = FullResult.of(db, system);
		var end = System.currentTimeMillis();
		System.out.println("Computed result in: "
				+ ((end - start) / 1000d));

		EnviFlow flow = result.enviIndex().at(42);

		start = System.currentTimeMillis();
		var sankey = Sankey.of(flow, result)
				.withMaximumNodeCount(50)
				.withMinimumShare(0.00)
				.build();
		end = System.currentTimeMillis();
		System.out.println("Computed sankey in: "
				+ ((end - start) / 1000d));

		System.out.println(sankey.toDot());

		db.close();
	}

}
