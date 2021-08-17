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

	public Point() {
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static Point fromJson(JsonObject obj) {
		return Coordinates.readPoint(
				obj.get("coordinates"));
	}

	@Override
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Point");
		obj.add("coordinates",
				Coordinates.writePoint(this));
		return obj;
	}

	@Override
	public Point copy() {
		Point clone = new Point();
		clone.x = x;
		clone.y = y;
		return clone;
	}

	@Override
	public String toString() {
		return "Point{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}

