package examples;

import java.io.File;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.io.UnitMapping;
import org.openlca.io.ecospold1.input.EcoSpold01Import;

public class EcoSpold1Import_Example {

	public static void main(String[] args) {

		// create a new database in the temporary files folder
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tmpDirPath + "/olca_test_db_1.4");

		try (DerbyDatabase database = new DerbyDatabase(tmpDir)) {

			// the folder with the EcoSpold 1 files
			String dirPath = "C:/Users/Dell/projects/data/ecoinvent/simple";
			File dir = new File(dirPath);

			// create the importer with default mappings for the units in the
			// reference data
			UnitMapping unitMapping = UnitMapping.createDefault(database);
			EcoSpold01Import importer = new EcoSpold01Import(database,
					unitMapping, dir.listFiles());
			importer.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
