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
		try {
			byte[] data = pack(coll);
			return BinUtils.gzip(data);
		} catch (IOException e) {
			Exceptions.unchecked(e);
			return null;
		}
	}

	public static byte[] pack(FeatureCollection coll) {
		var proto = Proto.FeatureCollection.newBuilder();
		for (var feature : coll.features) {
		}
		return proto.build().toByteArray();
	}

	private static Proto.Feature pack(Feature feature) {
		var proto = Proto.Feature.newBuilder();

		return proto.build();
	}

	private static Proto.Geometry pack(Geometry g) {
		var proto = Proto.Geometry.newBuilder();
		if (g instanceof Point) {
			proto.setPoint(pack((Point) g));
		}
		// TODO: type branches
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

	private static Proto.GeometryCollection pack(GeometryCollection geometryCollection) {
		var proto = Proto.GeometryCollection.newBuilder();
		// TODO: map values
		return proto.build();
	}
}
