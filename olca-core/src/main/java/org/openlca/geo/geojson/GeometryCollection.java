package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class GeometryCollection extends Geometry {

	public final List<Geometry> geometries = new ArrayList<>();

	public static GeometryCollection fromJson(JsonObject obj) {
		GeometryCollection coll = new GeometryCollection();
		JsonElement elem = obj.get("geometries");
		if (elem == null || !elem.isJsonArray())
			return coll;
		for (JsonElement gElem : elem.getAsJsonArray()) {
			if (!gElem.isJsonObject())
				continue;
			JsonObject gObj = gElem.getAsJsonObject();
			Geometry g = GeoJSON.readGeometry(gObj);
			if (g != null) {
				coll.geometries.add(g);
			}
		}
		return coll;
	}

	@Override
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "GeometryCollection");
		JsonArray array = new JsonArray();
		for (Geometry g : geometries) {
			if (g == null)
				continue;
			array.add(g.toJson());
		}
		obj.add("geometries", array);
		return obj;
	}

	@Override
	public GeometryCollection copy() {
		GeometryCollection c = new GeometryCollection();
		for (Geometry g : geometries) {
			if (g == null)
				continue;
			c.geometries.add(g.copy());
		}
		return c;
	}
}
