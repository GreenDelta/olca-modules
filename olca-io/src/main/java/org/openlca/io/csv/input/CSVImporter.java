package org.openlca.io.csv.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.parser.CSVParser;

public class CSVImporter {

	private ProcessImporter processImporter;
	private File file;
	private ReferenceDataImporter dataImporter;

	public CSVImporter(File file, IDatabase database) {
		this.file = file;
		processImporter = new ProcessImporter(database);
		dataImporter = new ReferenceDataImporter(database);
	}

	public void run() {

		CSVParser parser = new CSVParser(file);
		try {
			SPReferenceData referenceData = parser.start();
			CSVImportCache cache = dataImporter.importData(referenceData);
			processImporter.setCash(cache);
			processImporter.resetUnitMapping();
			while (parser.hasNext()) {
				SPDataEntry dataEntry = parser.next();
				if (dataEntry instanceof SPProcess)
					processImporter.runImport((SPProcess) dataEntry);
				else if (dataEntry instanceof SPWasteTreatment)
					processImporter.runImport((SPWasteTreatment) dataEntry);
				else
					throw new IllegalArgumentException("TODO");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
