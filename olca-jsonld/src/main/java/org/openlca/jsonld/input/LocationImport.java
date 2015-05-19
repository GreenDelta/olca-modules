package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private ImportConfig conf;

	private LocationImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static Location run(String refId, ImportConfig conf) {
		return new LocationImport(refId, conf).run();
	}

	private Location run() {
		if (refId == null || conf == null)
			return null;
		try {
			Location loc = conf.db.getLocation(refId);
			if (loc != null)
				return loc;
			JsonObject json = conf.store.get(ModelType.LOCATION, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import location " + refId, e);
			return null;
		}
	}

	private Location map(JsonObject json) {
		if (json == null)
			return null;
		Location loc = new Location();
		In.mapAtts(json, loc);
		loc.setCode(In.getString(json, "code"));
		loc.setLatitude(In.getDouble(json, "latitude", 0));
		loc.setLongitude(In.getDouble(json, "longitude", 0));
		loc = conf.db.put(loc);
		return loc;
	}
}
