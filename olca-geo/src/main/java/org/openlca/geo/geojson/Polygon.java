package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * As in the GeoJSON specification a polygon has a list / an array of
 * one or more linear rings where the first ring is the exterior ring
 * and the other rings optional interior rings.
 */
public final class Polygon extends Geometry {

	public final List<LineString> rings;

	Polygon(List<LineString> rings) {
		this.rings = rings;
	}

	public Polygon() {
		this(new ArrayList<>());
	}

	static Polygon fromJson(JsonObject obj) {
		return Coordinates.readPolygon(
				obj.get("coordinates"));
	}

	@Override
	JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Polygon");
		obj.add("coordinates",
				Coordinates.writePolygon(this));
		return obj;
	}
}
