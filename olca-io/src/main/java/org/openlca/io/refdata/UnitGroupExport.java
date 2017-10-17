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
		line[0] = group.getRefId();
		line[1] = group.getName();
		line[2] = group.getDescription();
		if (group.getCategory() != null)
			line[3] = group.getCategory().getRefId();
		if (group.getDefaultFlowProperty() != null)
			line[4] = group.getDefaultFlowProperty().getRefId();
		if (group.getReferenceUnit() != null)
			line[5] = group.getReferenceUnit().getRefId();
		return line;
	}
}
