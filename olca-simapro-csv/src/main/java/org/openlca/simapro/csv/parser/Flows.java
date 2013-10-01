package org.openlca.simapro.csv.parser;

import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

final class Flows {

	static SPElementaryFlow parseElementaryFlow(String line,
			String csvSeperator, ElementaryFlowType type)
			throws CSVParserException {

		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		if (split.length < 9)
			throw new CSVParserException("Error in " + type.getValue()
					+ " line: " + line);

		String name = split[0];
		String subCompartment = split[1];
		String unit = split[2];
		String formula = split[3];
		String distribution = split[4];
		String dValue1 = Utils.formatNumber(split[5]);
		String dValue2 = Utils.formatNumber(split[6]);
		String dValue3 = Utils.formatNumber(split[7]);
		String comment = split[8];
		
		for (int i = 9; i < (split.length - 1); i++) {
			comment += csvSeperator + split[i];
		}
		return new SPElementaryFlow(type,
				SubCompartment.forValue(subCompartment), name, unit, formula,
				comment, Utils.createDistibution(distribution, dValue1,
						dValue2, dValue3, comment));
	}

	static SPSubstance parseSubstance(String line, String csvSeperator,
			ElementaryFlowType type) {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		String name = split[0];
		String referenceUnit = split[1];
		String cas = split[2];
		String comment = split[3];

		for (int i = 4; i < (split.length - 1); i++) {
			comment += csvSeperator + split[i];
		}

		SPSubstance substance = new SPSubstance(name, referenceUnit);
		substance.setReferenceUnit(referenceUnit);
		substance.setCASNumber(cas);
		substance.setComment(comment);
		substance.setFlowType(type);

		return substance;
	}

}
