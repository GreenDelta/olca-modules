package org.openlca.simapro.csv.parser;

import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.parser.exception.CSVParserException;

public class Unit {

	static SPUnit parse(String line, String csvSeperator)
			throws CSVParserException {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 4)
			throw new CSVParserException("Error in quantity line: " + line);

		String name = split[0];
		String quantityName = split[1];
		String conversionFactor = split[2].replace(",", ".");
		String referenceUnit = split[3];

		for (int i = 4; i < (split.length - 1); i++) {
			referenceUnit += csvSeperator + split[i];
		}

		SPUnit unit = new SPUnit(name);
		unit.setConversionFactor(Double.parseDouble(Utils.replaceCSVSeperator(
				conversionFactor, csvSeperator)));
		unit.setReferenceUnit(referenceUnit);
		unit.setQuantity(quantityName);

		return unit;
	}

}
