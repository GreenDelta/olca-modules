package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.julia.Julia;

public class LibraryExport {

	public static void main(String[] args) throws Exception {
		var libPath = "C:/Users/Win10/Projects/openLCA/builds/olcar/olcar_1.0.0_windows_2019-10-07";
		var dbPath = "C:/Users/Win10/openLCA-data-1.4/databases/ecoinvent_36_cutoff_unit_20191212";
		var expPath = "C:/Users/Win10/Desktop/rems/ei3";

		Julia.loadFromDir(new File(libPath));
		var db = new DerbyDatabase(new File(dbPath));

		System.out.println("Start export");
		long start = System.currentTimeMillis();
		new org.openlca.core.library.LibraryExport(db, new File(expPath))
				//.solver(new JuliaSolver())
				.run();
		double time = (System.currentTimeMillis() - start) / 1000d;
		System.out.println("Done, it took " + String.format("%.0f seconds", time));
		db.close();
	}
}
