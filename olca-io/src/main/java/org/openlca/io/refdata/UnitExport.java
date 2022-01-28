package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class UnitExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		log.trace("write units");
		UnitGroupDao dao = new UnitGroupDao(db);
		int count = 0;
		for (UnitGroup unitGroup : dao.getAll()) {
			for (Unit unit : unitGroup.units) {
				Object[] line = createLine(unitGroup, unit);
				printer.printRecord(line);
			}
			count++;
		}
		log.trace("{} units written", count);
	}

	private Object[] createLine(UnitGroup group, Unit unit) {
		Object[] line = new Object[6];
		line[0] = unit.refId;
		line[1] = unit.name;
		line[2] = unit.description;
		line[3] = unit.conversionFactor;
		line[4] = unit.synonyms;
		line[5] = group.refId;
		return line;
	}
}
