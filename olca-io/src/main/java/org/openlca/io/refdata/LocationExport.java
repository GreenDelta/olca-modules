package org.openlca.io.refdata;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;

class LocationExport extends AbstractExport {

	@Override
	protected void doIt(CSVPrinter printer, IDatabase db) throws IOException {
		log.trace("write locations");
		LocationDao dao = new LocationDao(db);
		List<Location> locations = dao.getAll();
		for (Location location : locations) {
			Object[] line = createLine(location);
			printer.printRecord(line);
		}
		log.trace("{} locations written", locations.size());
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
