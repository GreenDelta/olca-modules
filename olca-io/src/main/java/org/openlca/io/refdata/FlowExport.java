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
		line[0] = flow.getRefId();
		line[1] = flow.getName();
		line[2] = flow.getDescription();
		if (flow.getCategory() != null)
			line[3] = flow.getCategory().getRefId();
		if (flow.getFlowType() != null)
			line[4] = flow.getFlowType().name();
		line[5] = flow.getCasNumber();
		line[6] = flow.getFormula();
		if (flow.getReferenceFlowProperty() != null)
			line[7] = flow.getReferenceFlowProperty().getRefId();
		return line;
	}

}
