package org.openlca.io.refdata;

import java.util.List;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.supercsv.io.CsvListWriter;

class FlowPropertyExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database) throws Exception {
		log.trace("write flow properties");
		FlowPropertyDao dao = new FlowPropertyDao(database);
		List<FlowProperty> properties = dao.getAll();
		for (FlowProperty property : properties) {
			Object[] line = createLine(property);
			writer.write(line);
		}
		log.trace("{} flow properties written", properties.size());
	}

	private Object[] createLine(FlowProperty property) {
		Object[] line = new Object[6];
		line[0] = property.getRefId();
		line[1] = property.getName();
		line[2] = property.getDescription();
		if (property.getCategory() != null)
			line[3] = property.getCategory().getRefId();
		if (property.getUnitGroup() != null)
			line[4] = property.getUnitGroup().getRefId();
		if (property.getFlowPropertyType() != null) {
			int type = property.getFlowPropertyType().ordinal();
			line[5] = type;
		}
		return line;
	}
}
