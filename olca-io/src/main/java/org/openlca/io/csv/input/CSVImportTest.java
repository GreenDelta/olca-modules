package org.openlca.io.csv.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;

public class CSVImportTest {

	public static void main(String[] args) throws Exception {
		IDatabase database = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/olca14CSV", "root", "");

		File file = new File("/Users/imo/Downloads/TestUnitError.CSV");
		File files[] = new File[1];
		files[0] = file;
		CSVImporter importer = new CSVImporter(database, files);
		importer.run();
		database.close();
	}
}
