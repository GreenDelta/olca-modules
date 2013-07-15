package org.openlca.io.samples;

import java.io.File;

import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.io.UnitMapping;
import org.openlca.io.ecospold1.importer.EcoSpold01Import;

public class EcoSpold1ImportSample {

	public static void main(String[] args) {

		// create a new database in the temporary files folder
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tmpDirPath + "/olca_test_db_1.4");
		boolean isNew = !tmpDir.exists();

		try (DerbyDatabase database = new DerbyDatabase(tmpDir)) {

			// if this is a new database we fill it with all reference data
			if (isNew)
				database.fill(DatabaseContent.ALL_REF_DATA);

			// the folder with the EcoSpold 1 files
			String dirPath = "C:/Users/Dell/projects/data/ecoinvent/simple";
			File dir = new File(dirPath);

			// create the importer with default mappings for the units in the
			// reference data
			UnitMapping unitMapping = UnitMapping.createDefault(database);
			EcoSpold01Import importer = new EcoSpold01Import(database,
					unitMapping);

			// import the files
			for (File file : dir.listFiles())
				importer.run(file, true); // true -> means process file; false
											// -> LCIA method

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
