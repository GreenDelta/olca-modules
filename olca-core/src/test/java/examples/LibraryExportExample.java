package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.library.LibraryExport;
import org.openlca.core.model.AllocationMethod;
import org.openlca.nativelib.NativeLib;

public class LibraryExportExample {

	public static void main(String[] args) throws Exception {
		NativeLib.loadFrom(DataDir.get().root());
		try (var db = DataDir.get().openDatabase("ei22")) {
			Upgrades.on(db);
			System.out.println("Start export");
			long start = System.currentTimeMillis();
			new LibraryExport(db, new File("target/data/lib"))
				.withUncertainties(true)
				.withAllocation(AllocationMethod.PHYSICAL)
				.run();
			double time = (System.currentTimeMillis() - start) / 1000d;
			System.out.println(
				"Done, it took " + String.format("%.0f seconds", time));
		}
	}
}
