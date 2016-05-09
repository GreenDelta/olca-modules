package org.openlca.jsonld.input;

import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.util.BinUtils;
import org.openlca.util.Geometries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		In.mapAtts(json, loc, id, conf);
		loc.setCode(In.getString(json, "code"));
		double latitude = In.getDouble(json, "latitude", 0);
		double longitude = In.getDouble(json, "longitude", 0);
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		if (!addGeometry(json, loc)) {
			if (latitude != 0d || longitude != 0d) {
				String kml = Geometries.pointToKml(latitude, longitude);
				loc.setKmz(Geometries.kmlToKmz(kml));
			}
		}
		loc = conf.db.put(loc);
		return loc;
	}

	private boolean addGeometry(JsonObject json, Location loc) {
		try {
			JsonObject geoJson = In.getObject(json, "geometry");
			if (geoJson == null)
				return false;
			String kml = GeoJson2Kml.convert(geoJson);
			byte[] kmz = BinUtils.zip(kml.getBytes("utf-8"));
			loc.setKmz(kmz);
			return true;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert GeoJson", e);
			return false;
		}
	}
}
