package org.openlca.jsonld.output;

import com.google.gson.JsonObject;
import org.openlca.core.model.Location;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.ProtoPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationWriter extends Writer<Location> {

	LocationWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	public JsonObject write(Location location) {
		JsonObject obj = super.write(location);
		if (obj == null)
			return null;
		Out.put(obj, "code", location.code);
		Out.put(obj, "latitude", location.latitude);
		Out.put(obj, "longitude", location.longitude);
		mapGeometry(location, obj);
		return obj;
	}

	private void mapGeometry(Location location, JsonObject obj) {
		if (location.geodata == null)
			return;
		try {
			FeatureCollection coll = ProtoPack.unpackgz(location.geodata);
			if (coll == null)
				return;
			Feature f = coll.first();
			if (f == null || f.geometry == null)
				return;
			Out.put(obj, "geometry", f.geometry.toJson());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert KML to GeoJSON", e);
		}
	}
}
