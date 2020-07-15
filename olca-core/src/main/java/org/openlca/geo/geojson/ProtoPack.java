package org.openlca.geo.geojson;

import java.io.IOException;

import org.openlca.util.BinUtils;
import org.openlca.util.Exceptions;

public class ProtoPack {

	private ProtoPack() {
	}

	/**
	 * Converts the given feature collection into a protocol buffer format and
	 * compresses it with gzip. This is the format that we use for storing
	 * geographic data of locations in an openLCA database.
	 */
	public static byte[] packgz(FeatureCollection coll) {
		if (coll == null)
			return null;
		try {
			byte[] data = pack(coll);
			return BinUtils.gzip(data);
		} catch (IOException e) {
			Exceptions.unchecked(e);
			return null;
		}
	}

	public static byte[] pack(FeatureCollection coll) {
		if (coll == null)
			return null;
		var proto = Proto.FeatureCollection.newBuilder();
		for (var feature : coll.features) {
			if (feature == null)
				continue;
			proto.addFeature(pack(feature));
		}
		return proto.build().toByteArray();
	}

	private static Proto.Feature pack(Feature feature) {
		var proto = Proto.Feature.newBuilder();
		if (feature.geometry != null) {
			proto.setGeometry(pack(feature.geometry));
		}
		if (feature.properties != null) {
			feature.properties.entrySet()
					.stream()
					.filter(e -> e.getKey() != null
							&& e.getValue() instanceof Number)
					.forEach(e -> proto.putProperties(
							e.getKey(),
							((Number) e.getValue()).doubleValue())
					);
		}
		return proto.build();
	}

	private static Proto.Geometry pack(Geometry g) {
		var proto = Proto.Geometry.newBuilder();
		if (g instanceof Point) {
			proto.setPoint(pack((Point) g));
		} else if (g instanceof MultiPoint) {
			proto.setMultiPoint(pack((MultiPoint) g));
		} else if (g instanceof LineString) {
			proto.setLineString(pack((LineString) g));
		} else if (g instanceof MultiLineString) {
			proto.setMultiLineString(pack((MultiLineString) g));
		} else if (g instanceof Polygon) {
			proto.setPolygon(pack((Polygon) g));
		} else if (g instanceof MultiPolygon) {
			proto.setMultiPolygon(pack((MultiPolygon) g));
		} else if (g instanceof GeometryCollection) {
			proto.setGeometryCollection(pack((GeometryCollection) g));
		}
		return proto.build();
	}

	private static Proto.Point pack(Point point) {
		var proto = Proto.Point.newBuilder();
		proto.setX(point.x);
		proto.setY(point.y);
		return proto.build();
	}

	private static Proto.MultiPoint pack(MultiPoint multiPoint) {
		var proto = Proto.MultiPoint.newBuilder();
		for (var point : multiPoint.points) {
			proto.addPoint(pack(point));
		}
		return proto.build();
	}

	private static Proto.LineString pack(LineString lineString) {
		var proto = Proto.LineString.newBuilder();
		for (var point : lineString.points) {
			proto.addPoint(pack(point));
		}
		return proto.build();
	}

	private static Proto.MultiLineString pack(MultiLineString multiLineString) {
		var proto = Proto.MultiLineString.newBuilder();
		for (var lineString : multiLineString.lineStrings) {
			proto.addLineString(pack(lineString));
		}
		return proto.build();
	}

	private static Proto.Polygon pack(Polygon polygon) {
		var proto = Proto.Polygon.newBuilder();
		for (var ring : polygon.rings) {
			proto.addRing(pack(ring));
		}
		return proto.build();
	}

	private static Proto.MultiPolygon pack(MultiPolygon multiPolygon) {
		var proto = Proto.MultiPolygon.newBuilder();
		for (var polygon : multiPolygon.polygons) {
			proto.addPolygon(pack(polygon));
		}
		return proto.build();
	}

	private static Proto.GeometryCollection pack(GeometryCollection coll) {
		var proto = Proto.GeometryCollection.newBuilder();
		for (var geometry : coll.geometries) {
			proto.addGeometry(pack(geometry));
		}
		return proto.build();
	}
}
