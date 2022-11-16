package org.openlca.io.refdata;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;

class FlowExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		FlowDao dao = new FlowDao(db);
		List<Flow> flows = dao.getAll();
		for (Flow flow : flows) {
			Object[] line = createLine(flow);
			printer.printRecord(line);
		}
	}

	private Object[] createLine(Flow flow) {
		Object[] line = new Object[8];
		line[0] = flow.refId;
		line[1] = flow.name;
		line[2] = flow.description;
		if (flow.category != null)
			line[3] = flow.category.refId;
		if (flow.flowType != null)
			line[4] = flow.flowType.name();
		line[5] = flow.casNumber;
		line[6] = flow.formula;
		if (flow.referenceFlowProperty != null)
			line[7] = flow.referenceFlowProperty.refId;
		return line;
	}

}
