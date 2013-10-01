package org.openlca.simapro.csv;

import java.io.File;
import java.io.FileWriter;

import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.parser.CSVParser;
import org.openlca.simapro.csv.writer.CSVWriter;

public class CSVWriterTest {

	public static void main(String[] args) {
		File file = new File("/Users/Imo/Desktop/test.csv");
		File inFile = new File("/Users/Imo/Desktop/testProcess.csv");
		try {
			CSVParser parser = new CSVParser(inFile);
			SPReferenceData referenceData = parser.start();
			SPDataEntry dataEntry = parser.next();

			SPProcess process = null;
			if (dataEntry instanceof SPProcess)
				process = (SPProcess) dataEntry;
			CSVWriter csvWriter = new CSVWriter(new FileWriter(file),
					CSVSeperator.TAB, '.');
			csvWriter.writeHeader("Test");
			csvWriter.write(process);
			csvWriter.write(referenceData);
			csvWriter.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
