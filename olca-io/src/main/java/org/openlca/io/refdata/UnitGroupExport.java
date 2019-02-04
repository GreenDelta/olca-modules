package org.openlca.io.refdata;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.UnitGroup;
import org.supercsv.io.CsvListWriter;

class UnitGroupExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database)
			throws Exception {
		log.trace("write unit groups");
		UnitGroupDao dao = new UnitGroupDao(database);
		List<UnitGroup> groups = dao.getAll();
		for (UnitGroup group : groups) {
			Object[] line = createLine(group);
			writer.write(line);
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
