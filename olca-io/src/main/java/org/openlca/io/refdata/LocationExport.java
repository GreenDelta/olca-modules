package org.openlca.io.refdata;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.supercsv.io.CsvListWriter;

class LocationExport extends AbstractExport {

	@Override
	protected void doIt(CsvListWriter writer, IDatabase database) throws Exception {
		log.trace("write locations");
		LocationDao dao = new LocationDao(database);
		List<Location> locations = dao.getAll();
		for (Location location : locations) {
			Object[] line = createLine(location);
			writer.write(line);
		}
		log.trace("{} locations written", locations.size());
	}

	private Object[] createLine(Location location) {
		Object[] line = new Object[6];
		line[0] = location.getRefId();
		line[1] = location.getName();
		line[2] = location.getDescription();
		line[3] = location.getCode();
		line[4] = location.getLatitude();
		line[5] = location.getLongitude();
		return line;
	}

}
