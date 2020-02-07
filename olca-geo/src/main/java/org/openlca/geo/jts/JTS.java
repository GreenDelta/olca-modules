package org.openlca.geo.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.openlca.geo.geojson.GeometryCollection;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.MultiLineString;
import org.openlca.geo.geojson.MultiPoint;
import org.openlca.geo.geojson.MultiPolygon;
import org.openlca.geo.geojson.Point;
import org.openlca.geo.geojson.Polygon;

public class JTS {

	/**
	 * Converts the given GeoJSON geometry into a corresponding JTS
	 * representation.
	 */
	public static Geometry fromGeoJSON(org.openlca.geo.geojson.Geometry g) {
		if (g == null)
			return null;
		GeometryFactory gen = new GeometryFactory();

		if (g instanceof Point)
			return point((Point) g, gen);

		if (g instanceof MultiPoint)
			return multiPoint((MultiPoint) g, gen);

		if (g instanceof LineString)
			return line((LineString) g, gen);

		if (g instanceof MultiLineString)
			return multiLine((MultiLineString) g, gen);

		if (g instanceof Polygon)
			return polygon((Polygon) g, gen);

		if (g instanceof MultiPolygon)
			return multiPolygon((MultiPolygon) g, gen);

		if (g instanceof GeometryCollection) {
			Geometry[] geoms = ((GeometryCollection) g).geometries.stream()
					.map(JTS::fromGeoJSON)
					.toArray(Geometry[]::new);
			return gen.createGeometryCollection(geoms);
		}

		return null;
	}

	private static Geometry point(Point p, GeometryFactory gen) {
		Coordinate coordinate = new Coordinate(p.x, p.y);
		return gen.createPoint(coordinate);
	}

	private static Geometry multiPoint(MultiPoint mp, GeometryFactory gen) {
		Coordinate[] coordinates = mp.points.stream()
				.map(p -> new Coordinate(p.x, p.y))
				.toArray(Coordinate[]::new);
		return gen.createMultiPoint(coordinates);
	}

	private static com.vividsolutions.jts.geom.LineString line(
			LineString line, GeometryFactory gen) {
		Coordinate[] coordinates = line.points.stream()
				.map(p -> new Coordinate(p.x, p.y))
				.toArray(Coordinate[]::new);
		return gen.createLineString(coordinates);
	}

	private static Geometry multiLine(MultiLineString ml, GeometryFactory gen) {
		com.vividsolutions.jts.geom.LineString[] lines = ml.lineStrings.stream()
				.map(line -> line(line, gen))
				.toArray(com.vividsolutions.jts.geom.LineString[]::new);
		return gen.createMultiLineString(lines);
	}

	private static com.vividsolutions.jts.geom.Polygon polygon(
			Polygon p, GeometryFactory gen) {
		if (p.rings.isEmpty())
			return null;
		Coordinate[] outer = p.rings.get(0).points.stream()
				.map(point -> new Coordinate(point.x, point.y))
				.toArray(Coordinate[]::new);
		LinearRing outerRing = gen.createLinearRing(outer);
		if (p.rings.size() == 1)
			return gen.createPolygon(outerRing);
		LinearRing[] innerRings = new LinearRing[p.rings.size() - 1];
		for (int i = 1; i < p.rings.size(); i++) {
			Coordinate[] ring = p.rings.get(i).points.stream()
					.map(point -> new Coordinate(point.x, point.y))
					.toArray(Coordinate[]::new);
			innerRings[i - 1] = gen.createLinearRing(ring);
		}
		return gen.createPolygon(outerRing, innerRings);
	}

	private static Geometry multiPolygon(MultiPolygon mp, GeometryFactory gen) {
		return gen.createMultiPolygon(mp.polygons
				.stream()
				.map(p -> polygon(p, gen))
				.toArray(com.vividsolutions.jts.geom.Polygon[]::new));
	}
}
