package org.openlca.io.refdata;

import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.supercsv.io.CsvListWriter;

class FlowExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database) throws Exception {
		log.trace("write flows");
		FlowDao dao = new FlowDao(database);
		List<Flow> flows = dao.getAll();
		for (Flow flow : flows) {
			Object[] line = createLine(flow);
			writer.write(line);
		}
		log.trace("{} flows written", flows.size());
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
