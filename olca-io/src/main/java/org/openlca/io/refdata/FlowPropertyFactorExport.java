package org.openlca.io.refdata;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.supercsv.io.CsvListWriter;

class FlowPropertyFactorExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database) throws Exception {
		log.trace("write flow property factors");
		FlowDao dao = new FlowDao(database);
		int count = 0;
		for (Flow flow : dao.getAll()) {
			for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
				Object[] line = createLine(flow, factor);
				writer.write(line);
				count++;
			}
		}
		log.trace("{} flow property factors written", count);
	}

	private Object[] createLine(Flow flow, FlowPropertyFactor factor) {
		Object[] line = new Object[3];
		line[0] = flow.getRefId();
		if (factor.getFlowProperty() != null)
			line[1] = factor.getFlowProperty().getRefId();
		line[2] = factor.getConversionFactor();
		return line;
	}

}
