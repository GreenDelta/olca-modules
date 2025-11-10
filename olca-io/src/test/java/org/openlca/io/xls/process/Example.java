package org.openlca.io.xls.process;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import org.openlca.core.DataDir;
import org.openlca.core.database.ProcessDao;

public class Example {

	public static void main(String[] args) {
		// this example exports a random process from a database to an
		// Excel file and then imports the process from that file
		// into an empty database.
		var file = new File("target/example.xlsx");
		try (var db = DataDir.get().openDatabase("ei39_cutoff")) {
			var ps = new ProcessDao(db).getDescriptors();
			var rand = ThreadLocalRandom.current();
			var i = rand.nextInt(ps.size());
			XlsProcessWriter.of(db)
				.write(ps.get(i), file);
		}

		try (var db = DataDir.get().openDatabase("empty")) {
			var process = XlsProcessReader.of(db)
				.sync(file)
				.orElseThrow();
			System.out.println("transferred process: " + process.name);
		}
	}
}
