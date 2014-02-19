package org.openlca.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.csv.output.CSVExporter;

public class ExporterTest {

	public static void main(String[] args) {
		MySQLDatabase database = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/refdata", "root", "");
		ProcessDao processDao = new ProcessDao(database);
		File file = new File("/Users/imo/Daten/openLCAExport.csv");
		CSVExporter exporter = null;
		try {
			List<ProcessDescriptor> list = new ArrayList<>();
			list.add(processDao.getDescriptor(20982));
			exporter = new CSVExporter(database, file, ';', list);
			exporter.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
