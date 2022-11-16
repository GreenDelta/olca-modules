package org.openlca.io;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.io.refdata.RefDataImport;
import org.openlca.util.Dirs;

public class RefDataExample {

	public static void main(String[] args) {
		var refDir = new File("C:/Users/ms/Desktop/rems/refdata2");
		var dataDir = DataDir.get();
		Dirs.delete(dataDir.getDatabaseDir("refdata"));
		try (var db = dataDir.openDatabase("refdata")) {
			var start = System.nanoTime();
			// new RefDataImport(refDir, db).run();
			new RefDataImport(refDir, db).run();
			var ns = System.nanoTime() - start;
			var seconds = ((double) ns) / 1e9;
			System.out.printf("imported in %.3f seconds%n", seconds);
		}
	}
}
