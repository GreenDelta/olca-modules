package org.openlca.geo.geojson;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.openlca.core.model.Copyable;

public final class Feature implements Copyable<Feature> {

	public Geometry geometry;

	/**
	 * Additional properties of this feature as a set of key-value pairs. We
	 * currently only support primitive types like numbers, booleans, and
	 * strings when de-/serializing a feature.
	 */
	public Map<String, Object> properties;

	public static Feature fromJson(JsonObject obj) {
		Feature f = new Feature();
		JsonElement geoElem = obj.get("geometry");
		if (geoElem != null && geoElem.isJsonObject()) {
			f.geometry = GeoJSON.readGeometry(
					geoElem.getAsJsonObject());
		}

		JsonElement propElem = obj.get("properties");
		if (propElem == null || !propElem.isJsonObject())
			return f;
		f.properties = new HashMap<>();
		JsonObject props = propElem.getAsJsonObject();
		props.entrySet().forEach(prop -> {
			String key = prop.getKey();
			JsonElement elem = prop.getValue();
			if (elem == null || !elem.isJsonPrimitive())
				return;
			JsonPrimitive prim = elem.getAsJsonPrimitive();
			if (prim.isNumber()) {
				f.properties.put(key, prim.getAsDouble());
			} else if (prim.isBoolean()) {
				f.properties.put(key, prim.getAsBoolean());
			} else if (prim.isString()) {
				f.properties.put(key, prim.getAsString());
			}
		});

		return f;
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Feature");
		if (geometry != null) {
			obj.add("geometry", geometry.toJson());
		}
		if (properties != null) {
			obj.add("properties",
					new Gson().toJsonTree(properties));
		}
		return obj;
	}

	@Override
	public Feature copy() {
		Feature c = new Feature();
		if (geometry != null) {
			c.geometry = geometry.copy();
		}
		if (properties != null) {
			c.properties = new HashMap<>(properties);
		}
		return c;
	}


}
