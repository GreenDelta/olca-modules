package examples;

import org.openlca.core.database.Derby;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.library.DbLibrarySwap;
import org.openlca.core.library.LibraryDir;

public class DbLibrarySwapExample {

	public static void main(String[] args) {
		try (var db = Derby.fromDataDir("infinite_en15804")) {
			Upgrades.on(db);

			var lib = LibraryDir.getDefault()
				.getLibrary("en15804_00.00.001")
				.orElseThrow();

			var swap = new DbLibrarySwap(db, lib);
			var start = System.nanoTime();
			swap.run();
			var time = (System.nanoTime() - start) / 1e9;
			System.out.printf("Swapped library in %.3f seconds", time);
		}
	}

}
