package org.openlca.geo.geojson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * A package private utility class for reading and writing coordinates.
 */
final class Coordinates {

	private Coordinates() {
	}

	private static double readCoordinate(JsonElement elem) {
		if (elem == null || !elem.isJsonPrimitive())
			return 0;
		JsonPrimitive prim = elem.getAsJsonPrimitive();
		return prim.isNumber()
				? prim.getAsDouble()
				: 0;
	}

	static Point readPoint(JsonElement elem) {
		Point p = new Point();
		if (elem == null || !elem.isJsonArray())
			return p;
		JsonArray array = elem.getAsJsonArray();
		if (array.size() < 2)
			return p;
		p.x = readCoordinate(array.get(0));
		p.y = readCoordinate(array.get(1));
		return p;
	}

	static List<Point> readPoints(JsonElement elem) {
		if (elem == null || !elem.isJsonArray())
			return Collections.emptyList();
		List<Point> points = new ArrayList<>();
		for (JsonElement e : elem.getAsJsonArray()) {
			Point point = readPoint(e);
			points.add(point);
		}
		return points;
	}

	static LineString readLine(JsonElement elem) {
		List<Point> points = readPoints(elem);
		return new LineString(points);
	}

	static List<LineString> readLines(JsonElement elem) {
		if (elem == null || !elem.isJsonArray())
			return Collections.emptyList();
		List<LineString> lines = new ArrayList<>();
		for (JsonElement e : elem.getAsJsonArray()) {
			lines.add(readLine(e));
		}
		return lines;
	}

	static Polygon readPolygon(JsonElement elem) {
		List<LineString> rings = readLines(elem);
		return new Polygon(rings);
	}

	static List<Polygon> readPolygons(JsonElement elem) {
		if (elem == null || !elem.isJsonArray())
			return Collections.emptyList();
		List<Polygon> polygons = new ArrayList<>();
		for (JsonElement e : elem.getAsJsonArray()) {
			polygons.add(readPolygon(e));
		}
		return polygons;
	}

	static JsonArray writePoint(Point point) {
		JsonArray array = new JsonArray();
		if (point == null)
			return array;
		array.add(new JsonPrimitive(point.x));
		array.add(new JsonPrimitive(point.y));
		return array;
	}

	static JsonArray writePoints(List<Point> points) {
		JsonArray array = new JsonArray();
		if (points == null)
			return array;
		for (Point p : points) {
			JsonArray pa = writePoint(p);
			array.add(pa);
		}
		return array;
	}

	static JsonArray writeLine(LineString line) {
		if (line == null)
			return new JsonArray();
		return writePoints(line.points);
	}

	static JsonArray writeLines(List<LineString> lines) {
		JsonArray array = new JsonArray();
		if (lines == null)
			return array;
		for (LineString line : lines) {
			array.add(writeLine(line));
		}
		return array;
	}

	static JsonArray writePolygon(Polygon polygon) {
		if (polygon == null)
			return new JsonArray();
		return writeLines(polygon.rings);
	}

	static JsonArray writePolygons(List<Polygon> polygons) {
		JsonArray array = new JsonArray();
		if (polygons == null)
			return array;
		for (Polygon polygon : polygons) {
			array.add(writePolygon(polygon));
		}
		return array;
	}
}
