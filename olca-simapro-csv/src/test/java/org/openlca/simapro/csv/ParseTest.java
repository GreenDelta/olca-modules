package org.openlca.simapro.csv;

import java.io.File;
import java.util.Map;

import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPWasteScenario;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.parser.CSVParser;

public class ParseTest {

	public static void main(String[] args) {
		File file = new File("/Users/imo/Desktop/testProcess.csv");
		CSVParser parser = new CSVParser(file);

		try {
			parser.start();
			Map<String, String[]> index = parser.getIndex();

			for (Map.Entry<String, String[]> entry : index.entrySet()) {
				System.out.println(entry.getKey());
				for (String s : entry.getValue())
					System.out.println(s);
				System.out.println();
			}

			while (parser.hasNext()) {
				SPDataEntry dataEntry = parser.next();
				if (dataEntry instanceof SPProcess
						|| dataEntry instanceof SPWasteTreatment) {
					System.out.println(dataEntry.getDocumentation().getName());
				} else if (dataEntry instanceof SPWasteScenario) {
					System.out.println("Waste Scenario");
				}
			}
			parser.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
