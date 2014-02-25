package org.openlca.simapro.csv.parser;

import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.enums.ParameterType;

final class Parameter {

	static SPCalculatedParameter parseCalculatedParameter(String line,
			String csvSeperator, ParameterType type) {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		String name = split[0];
		String expression = split[1];
		String comment = split[2];

		for (int i = 3; i < (split.length - 1); i++)
			comment += csvSeperator + split[i];

		SPCalculatedParameter parameter = new SPCalculatedParameter(name,
				expression, comment);
		parameter.setType(type);
		return parameter;
	}

	static SPInputParameter parseInputParameter(String line,
			String csvSeperator, ParameterType type) {
		line += csvSeperator + " ";
		String split[] = line.split(csvSeperator);
		SPInputParameter parameter = new SPInputParameter();
		parameter.setName(split[0]);
		String value = Utils.formatNumber(split[1]);
		parameter.setValue(Double.parseDouble(value));

		String distribution = split[2];
		String dValue1 = Utils.formatNumber(split[3]);
		String dValue2 = Utils.formatNumber(split[4]);
		String dValue3 = Utils.formatNumber(split[5]);

		String comment = split[7];
		for (int i = 8; i < (split.length - 1); i++)
			comment += csvSeperator + split[i];

		String hidden = split[6];
		parameter.setHidden("Yes".equals(hidden));
		parameter.setDistribution(Utils.createDistibution(distribution,
				dValue1, dValue2, dValue3, comment));
		parameter.setComment(comment);
		parameter.setType(type);
		return parameter;
	}
}
