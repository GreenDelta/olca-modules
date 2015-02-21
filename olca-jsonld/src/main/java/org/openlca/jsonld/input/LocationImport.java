package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private EntityStore store;
	private Db db;

	private LocationImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	static Location run(String refId, EntityStore store, Db db) {
		return new LocationImport(refId, store, db).run();
	}

	private Location run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			Location loc = db.getLocation(refId);
			if (loc != null)
				return loc;
			JsonObject json = store.get(ModelType.LOCATION, refId);
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
		loc = db.put(loc);
		return loc;
	}
}
