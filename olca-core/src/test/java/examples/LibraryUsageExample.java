package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.LibraryUsage;

public class LibraryUsageExample {

	public static void main(String[] args) {
		var dir = DataDir.get().getDatabasesDir();
		var lib = "ecoinvent v3.10 EN15804GD Unit-Processes 2024-10-23";
		var dbs = LibraryUsage
			.allDatabasesOf(dir, lib)
			.orElseThrow();

		if(dbs.isEmpty()) {
			System.out.println("Library not used: " + lib);
			return;
		}

		System.out.printf("Library %s used in %d database(s):%n", lib, dbs.size());
		for (var db : dbs) {
			System.out.println("  - " + db);
		}
	}
}
