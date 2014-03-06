package org.openlca.simapro.csv.reader;

import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.enums.ParameterType;

final class Parameter {

	static CalculatedParameterRow parseCalculatedParameter(String line,
			String csvSeperator, ParameterType type) {
		CalculatedParameterRow param = CalculatedParameterRow.fromCsv(line,
				csvSeperator);
		param.setType(type);
		return param;
	}

	static InputParameterRow parseInputParameter(String line,
			String csvSeperator, ParameterType type) {
		InputParameterRow param = InputParameterRow.fromCsv(line, csvSeperator);
		param.setType(type);
		return param;
	}
}
