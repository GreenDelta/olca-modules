package org.openlca.jsonld.input;

import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

class LocationImport extends BaseImport<Location> {

	private LocationImport(String refId, ImportConfig conf) {
		super(ModelType.LOCATION, refId, conf);
	}

	static Location run(String refId, ImportConfig conf) {
		return new LocationImport(refId, conf).run();
	}

	@Override
	Location map(JsonObject json, long id) {
		if (json == null)
			return null;
		Location loc = new Location();
		loc.setId(id);
		In.mapAtts(json, loc);
		loc.setCode(In.getString(json, "code"));
		loc.setLatitude(In.getDouble(json, "latitude", 0));
		loc.setLongitude(In.getDouble(json, "longitude", 0));
		loc = conf.db.put(loc);
		return loc;
	}
}
