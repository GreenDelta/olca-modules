package org.openlca.geo;

import org.openlca.geo.geojson.Geometry;
import org.openlca.geo.geojson.GeometryCollection;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.MultiLineString;
import org.openlca.geo.geojson.MultiPolygon;
import org.openlca.geo.geojson.Point;
import org.openlca.geo.geojson.Polygon;

public enum Shape {

	POINT,

	LINE,

	POLYGON,

	MULTI_LINE,

	MULTI_POLYGON,

	COLLECTION,

	UNKNOWN;

	public static Shape of(Geometry g) {
		if (g == null)
			return UNKNOWN;
		if (g instanceof Point)
			return POINT;
		if (g instanceof LineString)
			return LINE;
		if (g instanceof Polygon)
			return POLYGON;
		if (g instanceof MultiLineString)
			return MULTI_LINE;
		if (g instanceof MultiPolygon)
			return MULTI_POLYGON;
		if (g instanceof GeometryCollection)
			return COLLECTION;
		return UNKNOWN;
	}

	@Override
	public String toString() {
		return switch (this) {
			case POINT -> "Point";
			case LINE -> "Line string";
			case POLYGON -> "Polygon";
			case MULTI_LINE -> "Multi-line string";
			case MULTI_POLYGON -> "Multi-polygon";
			case COLLECTION -> "Geometry collection";
			case UNKNOWN -> "Unknown geometry";
		};
	}
}
