package org.openlca.io.samples;

import java.io.File;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.ConnectionData;
import org.openlca.core.database.mysql.Database;
import org.openlca.io.ecospold2.EcoSpold2Import;

/**
 * A sample for importing an EcoSpold 02 database into openLCA via API.
 */
public class EcoSpold2ImportSample {

	public static void main(String[] args) {

		// create a database connection
		ConnectionData data = new ConnectionData();
		data.setDatabase("ei3_test");
		data.setUser("root");
		data.setPersistenceProvider(new PersistenceProvider());

		try (IDatabase database = new Database(data)) {

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
