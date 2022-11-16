package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;

class FlowPropertyExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		var dao = new FlowPropertyDao(db);
		for (var property : dao.getAll()) {
			var line = createLine(property);
			printer.printRecord(line);
		}
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
