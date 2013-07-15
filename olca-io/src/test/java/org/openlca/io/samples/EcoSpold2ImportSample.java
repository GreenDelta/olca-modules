package org.openlca.io.samples;

import java.io.File;

import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.io.ecospold2.EcoSpold2Import;

/**
 * A sample for importing an EcoSpold 02 database into openLCA via API.
 */
public class EcoSpold2ImportSample {

	public static void main(String[] args) {

		// create a database
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tmpDirPath + "/olca_test_db_1.4");
		boolean isNew = !tmpDir.exists();

		try (DerbyDatabase database = new DerbyDatabase(tmpDir)) {

			if (isNew)
				database.fill(DatabaseContent.UNITS); // reference data for
														// units
														// and flow properties
			// run the import
			String dirPath = "C:/Users/Dell/projects/openlca/data/ecoinvent3/default/samples";
			File dir = new File(dirPath);
			EcoSpold2Import importer = new EcoSpold2Import(database);
			importer.run(dir.listFiles());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
