package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

class LocationImport implements Runnable {

	private final ImportConfig config;

	LocationImport(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var locations = new ArrayList<Location>();
		config.eachRowOf("locations.csv", row -> {
			var loc = new Location();
			loc.refId = row.get(0);
			loc.name = row.get(1);
			loc.description = row.get(2);
			loc.category = config.category(ModelType.LOCATION, row.get(3));
			loc.code = row.get(4);
			loc.latitude = row.getDouble(5);
			loc.longitude = row.getDouble(6);
			locations.add(loc);
		});
		config.insert(locations);
	}
}
