package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;

class FlowPropertyFactorExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		log.trace("write flow property factors");
		FlowDao dao = new FlowDao(db);
		int count = 0;
		for (Flow flow : dao.getAll()) {
			for (FlowPropertyFactor factor : flow.flowPropertyFactors) {
				Object[] line = createLine(flow, factor);
				printer.printRecord(line);
				count++;
			}
		}
		log.trace("{} flow property factors written", count);
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
