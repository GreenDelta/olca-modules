package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public final class LineString extends Geometry {

	public final List<Point> points;

	public LineString(List<Point> points) {
		this.points = points;
	}

	public LineString() {
		this(new ArrayList<>());
	}

	public static LineString fromJson(JsonObject obj) {
		return Coordinates.readLine(
				obj.get("coordinates"));
	}

	@Override
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "LineString");
		obj.add("coordinates",
				Coordinates.writeLine(this));
		return obj;
	}

	@Override
	public LineString copy() {
		if (points == null || points.isEmpty())
			return new LineString();
		List<Point> cPoints = new ArrayList<>();
		for (Point p : points) {
			if (p == null)
				continue;
			cPoints.add(p.copy());
		}
		return new LineString(cPoints);
	}

}
