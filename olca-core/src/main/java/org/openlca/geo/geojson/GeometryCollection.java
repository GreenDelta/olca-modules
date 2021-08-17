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

	/**
	 * If this geometry collection only contains points, line strings, or
	 * polygons, this method converts this collection into the corresponding
	 * homogeneous multi-geometry type (multi-point, multi-line-string, or
	 * multi-polygon). If this is not the case, the unchanged geometry collection
	 * is returned. This method is not thread-safe.
	 */
	public Geometry trySimplify() {
		int len = geometries.size();
		if (len == 0)
			return this;
		if (len == 1) {
			var first = geometries.get(0);
			return first instanceof GeometryCollection
				? ((GeometryCollection) first).trySimplify()
				: first;
		}

		// count the geometry types
		int points = 0;
		int lines = 0;
		int polygons = 0;
		for (var g : geometries) {
			if (g instanceof Point) {
				points++;
			} else if (g instanceof LineString) {
				lines++;
			} else if (g instanceof Polygon) {
				polygons++;
			}
		}

		// multi-point
		if (points > 0 && points == len) {
			var multiPoint = new MultiPoint();
			for (var g : geometries) {
				multiPoint.points.add((Point) g);
			}
			return multiPoint;
		}

		// multi-lines
		if (lines > 0 && lines == len) {
			var multiLine = new MultiLineString();
			for (var g : geometries) {
				multiLine.lineStrings.add((LineString) g);
			}
			return multiLine;
		}

		// multi-polygons
		if (polygons > 0 && polygons == len) {
			var multiPolygon = new MultiPolygon();
			for (var g : geometries) {
				multiPolygon.polygons.add((Polygon) g);
			}
			return multiPolygon;
		}

		return this;
	}
}
