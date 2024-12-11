package org.openlca.jsonld.output;

import org.openlca.core.model.Location;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public record LocationWriter(JsonExport exp) implements JsonWriter<Location> {

	@Override
	public JsonObject write(Location location) {
		var obj = Util.init(exp, location);
		Json.put(obj, "code", location.code);
		Json.put(obj, "latitude", location.latitude);
		Json.put(obj, "longitude", location.longitude);
		mapGeometry(location, obj);
		return obj;
	}

	private void mapGeometry(Location location, JsonObject obj) {
		if (location.geodata == null)
			return;
		try {
			FeatureCollection coll = GeoJSON.unpack(location.geodata);
			if (coll == null)
				return;
			Feature f = coll.first();
			if (f == null || f.geometry == null)
				return;
			Json.put(obj, "geometry", f.geometry.toJson());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert KML to GeoJSON", e);
		}
	}
}
