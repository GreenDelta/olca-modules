package org.openlca.io.maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.io.maps.content.ES2ElementaryFlowContent;

public class MappingBuilderImport {

	public static void main(String[] args) throws Exception {

		IDatabase database = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/ES2Mapping", "root", "");

		// File file = new File("/Users/imo/Downloads/productFlowsMap.csv");
		File file = new File(
				"/Users/imo/Downloads/ecoinvent3_uuids_mapping.csv");

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();

		MappingImporter importer = new MappingImporter(database);

		while (line != null) {
			String[] column = line.split(";");
			ES2ElementaryFlowContent content = new ES2ElementaryFlowContent();
			importer.addForExport(content, column[1],
					MapType.ES2_ELEMENTARY_FLOW);
			line = reader.readLine();
		}

		reader.close();

	}
}
