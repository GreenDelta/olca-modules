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
		if (elem == null || !elem.isJsonArray())
			return null;
		JsonArray array = elem.getAsJsonArray();
		if (array.size() < 2)
			return null;
		Point p = new Point();
		p.x = readCoordinate(array.get(0));
		p.y = readCoordinate(array.get(1));
		return p;
	}

	static List<Point> readLine(JsonElement elem) {
		return readPoints(elem);
	}

	static List<Point> readPoints(JsonElement elem) {
		if (elem == null || !elem.isJsonArray())
			return Collections.emptyList();
		List<Point> points = new ArrayList<>();
		for (JsonElement e : elem.getAsJsonArray()) {
			Point point = readPoint(e);
			if (e != null) {
				points.add(point);
			}
		}
		return points;
	}

	static JsonArray writePoint(Point point) {
		if (point == null)
			return null;
		JsonArray array = new JsonArray();
		array.add(new JsonPrimitive(point.x));
		array.add(new JsonPrimitive(point.y));
		return array;
	}





}
