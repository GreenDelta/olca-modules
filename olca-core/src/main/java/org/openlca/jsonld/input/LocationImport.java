package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.geo.geojson.ProtoPack;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		In.mapAtts(json, loc, id, conf);
		loc.code = Json.getString(json, "code");
		double latitude = Json.getDouble(json, "latitude", 0);
		double longitude = Json.getDouble(json, "longitude", 0);
		loc.latitude = latitude;
		loc.longitude = longitude;
		JsonObject geometry = Json.getObject(json, "geometry");
		if (geometry != null) {
			try {
				FeatureCollection coll = GeoJSON.read(geometry);
				if (coll != null) {
					loc.geodata = ProtoPack.packgz(coll);
				}
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("Failed to read geometry data from " + loc, e);
			}
		}
		loc = conf.db.put(loc);
		return loc;
	}
}
