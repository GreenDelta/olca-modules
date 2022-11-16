package org.openlca.io.refdata;

import java.io.IOException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;

class LocationExport implements Export {

	@Override
	public void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		var dao = new LocationDao(db);
		for (var location : dao.getAll()) {
			var line = createLine(location);
			printer.printRecord(line);
		}
	}

	private Object[] createLine(Location location) {
		Object[] line = new Object[6];
		line[0] = location.refId;
		line[1] = location.name;
		line[2] = location.description;
		line[3] = location.code;
		line[4] = location.latitude;
		line[5] = location.longitude;
		return line;
	}

}
