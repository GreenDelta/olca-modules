package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;

class FlowPropertyFactorExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		var dao = new FlowDao(db);
		for (var flow : dao.getAll()) {
			for (var factor : flow.flowPropertyFactors) {
				var line = createLine(flow, factor);
				printer.printRecord(line);
			}
		}
	}

	private Object[] createLine(Flow flow, FlowPropertyFactor factor) {
		Object[] line = new Object[3];
		line[0] = flow.refId;
		if (factor.flowProperty != null)
			line[1] = factor.flowProperty.refId;
		line[2] = factor.conversionFactor;
		return line;
	}

}
