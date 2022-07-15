package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Location;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.jsonld.Json;
import org.slf4j.LoggerFactory;

public record LocationReader(EntityResolver resolver)
	implements EntityReader<Location> {

	public LocationReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Location read(JsonObject json) {
		var location = new Location();
		update(location, json);
		return location;
	}

	@Override
	public void update(Location location, JsonObject json) {
		Util.mapBase(location, json, resolver);
		location.code = Json.getString(json, "code");
		double latitude = Json.getDouble(json, "latitude", 0);
		double longitude = Json.getDouble(json, "longitude", 0);
		location.latitude = latitude;
		location.longitude = longitude;
		var geometry = Json.getObject(json, "geometry");
		if (geometry != null) {
			try {
				FeatureCollection coll = GeoJSON.read(geometry);
				if (coll != null) {
					location.geodata = GeoJSON.pack(coll);
				}
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("Failed to read geometry data of " + location, e);
			}
		}
	}
}
