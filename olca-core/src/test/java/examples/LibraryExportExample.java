package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.library.LibraryExport;
import org.openlca.core.model.AllocationMethod;
import org.openlca.nativelib.NativeLib;

public class LibraryExportExample {

	public static void main(String[] args) throws Exception {
		NativeLib.loadFrom(DataDir.root());
		try (var db = Derby.fromDataDir("ei2")) {
			System.out.println("Start export");
			long start = System.currentTimeMillis();
			new LibraryExport(db, new File("target/data/lib"))
				.withInventory(true)
				.withImpacts(true)
				.withUncertainties(true)
				.withAllocation(AllocationMethod.PHYSICAL)
				.run();
			double time = (System.currentTimeMillis() - start) / 1000d;
			System.out.println(
				"Done, it took " + String.format("%.0f seconds", time));
		}
	}
}
