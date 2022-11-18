package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.Location;

class LocationExport implements Runnable {

	private final ExportConfig config;

	LocationExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var locations = config.db().getAll(Location.class);
		if (locations.isEmpty())
			return;
		config.sort(locations);
		var buffer = new ArrayList<>(7);

		config.writeTo("locations.csv", csv -> {

			// write column headers
			csv.printRecord(
					"ID",
					"Name",
					"Description",
					"Category",
					"Code",
					"Latitude",
					"Longitude");

			for (var location : locations) {
				buffer.add(location.refId);
				buffer.add(location.name);
				buffer.add(location.description);
				buffer.add(config.toPath(location.category));
				buffer.add(location.code);
				buffer.add(location.latitude);
				buffer.add(location.longitude);
				csv.printRecord(buffer);
				buffer.clear();
			}
		});
	}
}
