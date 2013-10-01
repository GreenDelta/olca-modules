package org.openlca.simapro.csv;

import java.io.File;
import java.io.IOException;

import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.parser.CSVParser;
import org.openlca.simapro.csv.parser.CSVParserException;

public class SimaProImport {

	public static void main(String[] args) {
		new SimaProImport().run();
	}

	public void run() {
		File file = new File("/Users/imo/Desktop/AllProcesses.csv");
		CSVParser parser = new CSVParser(file);
		try {
			SPReferenceData referenceData = parser.start();
						

		} catch (IOException | CSVParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
