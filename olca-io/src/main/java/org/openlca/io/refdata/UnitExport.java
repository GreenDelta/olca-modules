package org.openlca.io.refdata;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.supercsv.io.CsvListWriter;

class UnitExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database) throws Exception {
		log.trace("write units");
		UnitGroupDao dao = new UnitGroupDao(database);
		int count = 0;
		for (UnitGroup unitGroup : dao.getAll()) {
			for (Unit unit : unitGroup.getUnits()) {
				Object[] line = createLine(unitGroup, unit);
				writer.write(line);
			}
			count++;
		}
		log.trace("{} units written", count);
	}

	private Object[] createLine(UnitGroup group, Unit unit) {
		Object[] line = new Object[6];
		line[0] = unit.getRefId();
		line[1] = unit.getName();
		line[2] = unit.getDescription();
		line[3] = unit.getConversionFactor();
		line[4] = unit.getSynonyms();
		line[5] = group.getRefId();
		return line;
	}
}
