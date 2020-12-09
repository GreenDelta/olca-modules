package org.openlca.io.simapro.csv.input;

import java.util.List;
import java.util.stream.Collectors;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.util.Strings;

class Flows {

	static String getMappingID(
			ElementaryFlowType type, 
			ElementaryExchangeRow row) {
		if (row == null || type == null)
			return "";
		return List.of(
				row.name,
				type.getExchangeHeader(),
				row.subCompartment,
				row.unit)
				.stream()
				.map(Strings::orEmpty)
				.map(s -> s.trim().toLowerCase())
				.collect(Collectors.joining("/"));
	}
	
	

}
