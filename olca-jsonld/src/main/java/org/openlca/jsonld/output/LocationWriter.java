package org.openlca.jsonld.output;

import org.openlca.core.model.Location;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

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
		if (location.kmz == null)
			return;
		try {
			byte[] bin = BinUtils.unzip(location.kmz);
			String kml = new String(bin, "utf-8");
			JsonObject geoJson = Kml2GeoJson.convert(kml);
			Out.put(obj, "geometry", geoJson);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert KML to GeoJSON", e);
		}
	}
}
