package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public final class MultiPolygon extends Geometry {

	public final List<Polygon> polygons;

	MultiPolygon(List<Polygon> polygons) {
		this.polygons = polygons;
	}

	public MultiPolygon() {
		this(new ArrayList<>());
	}

	public static MultiPolygon fromJson(JsonObject obj) {
		List<Polygon> polygons = Coordinates.readPolygons(
				obj.get("coordinates"));
		return new MultiPolygon(polygons);
	}

	@Override
	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "MultiPolygon");
		obj.add("coordinates",
				Coordinates.writePolygons(polygons));
		return obj;
	}

	@Override
	public MultiPolygon copy() {
		MultiPolygon c = new MultiPolygon();
		if (polygons == null)
			return c;
		for (Polygon polygon : polygons) {
			if (polygon == null)
				continue;
			c.polygons.add(polygon.copy());
		}
		return c;
	}
}
