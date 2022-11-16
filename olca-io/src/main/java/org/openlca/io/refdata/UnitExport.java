package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class UnitExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		var dao = new UnitGroupDao(db);
		for (var group : dao.getAll()) {
			for (var unit : group.units) {
				var line = createLine(group, unit);
				printer.printRecord(line);
			}
		}
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
