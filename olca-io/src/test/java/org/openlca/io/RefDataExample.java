package org.openlca.io;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.io.refdata.RefDataExport;
import org.openlca.io.refdata.RefDataImport;
import org.openlca.util.Dirs;

public class RefDataExample {

	public static void main(String[] args) {
		runExport();
		// runImport();
	}

	private static void runExport() {
		var home = new File(System.getProperty("user.home"));
		var refDir = new File(home, "Desktop/rems/refdata2");
		var dataDir = DataDir.get();
		try (var db = dataDir.openDatabase("lciapack")) {
			var start = System.nanoTime();
			new RefDataExport(refDir, db).run();
			var ns = System.nanoTime() - start;
			var seconds = ((double) ns) / 1e9;
			System.out.printf("exported in %.3f seconds%n", seconds);
		}
	}

	private static void runImport() {
		var home = new File(System.getProperty("user.home"));
		var refDir = new File(home, "Desktop/rems/refdata2");
		var dataDir = DataDir.get();
		Dirs.delete(dataDir.getDatabaseDir("refdata"));
		try (var db = dataDir.openDatabase("refdata")) {
			var start = System.nanoTime();
			new RefDataImport(refDir, db).run();
			var ns = System.nanoTime() - start;
			var seconds = ((double) ns) / 1e9;
			System.out.printf("imported in %.3f seconds%n", seconds);
		}
	}
}
