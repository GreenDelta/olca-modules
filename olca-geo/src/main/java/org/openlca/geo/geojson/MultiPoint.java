package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public final class MultiPoint extends Geometry {

	public final List<Point> points;

	MultiPoint(List<Point> points) {
		this.points = points;
	}

	public MultiPoint() {
		this(new ArrayList<>());
	}

	static MultiPoint fromJson(JsonObject obj) {
		List<Point> points = Coordinates.readPoints(
				obj.get("coordinates"));
		return new MultiPoint(points);
	}

	@Override
	JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "MultiPoint");
		obj.add("coordinates",
				Coordinates.writePoints(points));
		return obj;
	}
}
