package org.openlca.io.samples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.io.ecospold2.EcoSpold2Import;

/**
 * A sample for importing an EcoSpold 02 database into openLCA via API.
 */
public class EcoSpold2ImportSample {

	public static void main(String[] args) {

		// create a database connection
		String url = "jdbc:mysql://localhost:3306/ei3_test";
		String user = "root";
		try (IDatabase database = new MySQLDatabase(url, user, "")) {

			// run the import
			String dirPath = "C:/Users/Dell/projects/openlca/data/ecoinvent3/default/datasets";
			File dir = new File(dirPath);
			EcoSpold2Import importer = new EcoSpold2Import(database);
			importer.run(dir.listFiles());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
