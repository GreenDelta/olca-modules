package org.openlca.io.refdata;

import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.UnitGroup;

class UnitGroupExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter printer, IDatabase db) throws Exception {
		log.trace("write unit groups");
		UnitGroupDao dao = new UnitGroupDao(db);
		List<UnitGroup> groups = dao.getAll();
		for (UnitGroup group : groups) {
			Object[] line = createLine(group);
			printer.printRecord(line);
		}
		log.trace("{} unit groups written", groups.size());
	}

	private Object[] createLine(UnitGroup group) {
		Object[] line = new Object[6];
		line[0] = group.refId;
		line[1] = group.name;
		line[2] = group.description;
		if (group.category != null)
			line[3] = group.category.refId;
		if (group.defaultFlowProperty != null)
			line[4] = group.defaultFlowProperty.refId;
		if (group.referenceUnit != null)
			line[5] = group.referenceUnit.refId;
		return line;
	}
}
