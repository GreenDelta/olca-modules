package org.openlca.core.library.csv;

import org.apache.commons.csv.CSVRecord;

public record LibTechEntry(
		int index,
		LibProcessInfo process,
		LibFlowInfo flow) {

	static LibTechEntry read(CSVRecord row) {
		var index = Csv.readInt(row, 0);
		var process = LibProcessInfo.read(row, 1);
		var flow = LibFlowInfo.read(row, 1 + LibProcessInfo.size());
		return new LibTechEntry(index, process, flow);
	}

}
