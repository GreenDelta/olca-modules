package org.openlca.simapro.csv.reader;

import org.openlca.simapro.csv.model.SPCalculatedParameter;
import org.openlca.simapro.csv.model.SPInputParameter;
import org.openlca.simapro.csv.model.enums.ParameterType;

final class Parameter {

	static SPCalculatedParameter parseCalculatedParameter(String line,
			String csvSeperator, ParameterType type) {
		SPCalculatedParameter param = SPCalculatedParameter.fromCsv(line,
				csvSeperator);
		param.setType(type);
		return param;
	}

	static SPInputParameter parseInputParameter(String line,
			String csvSeperator, ParameterType type) {
		SPInputParameter param = SPInputParameter.fromCsv(line, csvSeperator);
		param.setType(type);
		return param;
	}
}
