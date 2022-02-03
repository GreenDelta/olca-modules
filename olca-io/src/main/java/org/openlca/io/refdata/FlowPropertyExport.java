package org.openlca.io.refdata;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;

class FlowPropertyExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		log.trace("write flow properties");
		FlowPropertyDao dao = new FlowPropertyDao(db);
		List<FlowProperty> properties = dao.getAll();
		for (FlowProperty property : properties) {
			Object[] line = createLine(property);
			printer.printRecord(line);
		}
		log.trace("{} flow properties written", properties.size());
	}

	private Object[] createLine(FlowProperty property) {
		Object[] line = new Object[6];
		line[0] = property.refId;
		line[1] = property.name;
		line[2] = property.description;
		if (property.category != null)
			line[3] = property.category.refId;
		if (property.unitGroup != null)
			line[4] = property.unitGroup.refId;
		if (property.flowPropertyType != null) {
			int type = property.flowPropertyType.ordinal();
			line[5] = type;
		}
		return line;
	}
}
