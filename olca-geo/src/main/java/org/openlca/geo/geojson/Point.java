package org.openlca.geo.geojson;

import com.google.gson.JsonObject;

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
		Point p = Coordinates.readPoint(
				obj.get("coordinates"));
		return p != null ? p : new Point();
	}

	@Override
	JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Point");
		obj.add("coordinates",
				Coordinates.writePoint(this));
		return obj;
	}
}
