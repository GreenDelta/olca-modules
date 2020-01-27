package org.openlca.geo.geojson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class Point extends Geometry {

	/**
	 * The x-coordinate is the longitude of the point.
	 */
	public double x;

	/**
	 * The y-coordinate is the latitude of the point.
	 */
	public double y;

	static Point fromJson(JsonObject obj) {
		Point p = new Point();
		JsonArray coordinates = obj.get("coordinates").getAsJsonArray();
		if (coordinates.size() > 0) {
			p.x = coordinates.get(0).getAsDouble();
		}
		if (coordinates.size() > 1) {
			p.y = coordinates.get(1).getAsDouble();
		}
		return p;
	}

	JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Point");
		JsonArray coordinates = new JsonArray();
		coordinates.add(new JsonPrimitive(x));
		coordinates.add(new JsonPrimitive(y));
		obj.add("coordinates", coordinates);
		return obj;
	}
}
