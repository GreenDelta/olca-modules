package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.library.DbLibrarySwap;

public class DbLibrarySwapExample {

	public static void main(String[] args) {
		try (var db = DataDir.get().openDatabase("infinite_en15804")) {
			Upgrades.on(db);

			var lib = DataDir.get().getLibraryDir()
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
