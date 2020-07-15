package org.openlca.geo.geojson;

import java.io.IOException;
import java.util.HashMap;

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

	public static FeatureCollection unpackgz(byte[] data) {
		if (data == null || data.length == 0)
			return null;
		try {
			byte[] raw = BinUtils.gunzip(data);
			return unpack(raw);
		} catch (IOException e) {
			Exceptions.unchecked(e);
			return null;
		}
	}

	public static FeatureCollection unpack(byte[] data) {
		if (data == null)
			return null;
		try {
			var proto = Proto.FeatureCollection.parseFrom(data);
			var coll = new FeatureCollection();
			for (int i = 0; i < proto.getFeatureCount(); i++) {
				coll.features.add(unpack(proto.getFeature(i)));
			}
			return coll;
		} catch (Exception e) {
			Exceptions.unchecked(e);
			return null;
		}
	}

	private static Feature unpack(Proto.Feature proto) {
		var feature = new Feature();
		feature.geometry = unpack(proto.getGeometry());
		if (proto.getPropertiesCount() > 0) {
			feature.properties = new HashMap<>(proto.getPropertiesMap());
		}
		return feature;
	}

	private static Geometry unpack(Proto.Geometry proto) {
		if (proto.hasPoint())
			return unpack(proto.getPoint());
		if (proto.hasMultiPoint())
			return unpack(proto.getMultiPoint());
		if (proto.hasLineString())
			return unpack(proto.getLineString());
		if (proto.hasMultiLineString())
			return unpack(proto.getMultiLineString());
		if (proto.hasPolygon())
			return unpack(proto.getPolygon());
		if (proto.hasMultiPolygon())
			return unpack(proto.getMultiPolygon());
		if (proto.hasGeometryCollection())
			return unpack(proto.getGeometryCollection());
		return null;
	}

	private static Point unpack(Proto.Point proto) {
		var point = new Point();
		point.x = proto.getX();
		point.y = proto.getY();
		return point;
	}

	private static MultiPoint unpack(Proto.MultiPoint proto) {
		var multiPoint = new MultiPoint();
		for (int i = 0; i < proto.getPointCount(); i++) {
			var point = unpack(proto.getPoint(i));
			multiPoint.points.add(point);
		}
		return multiPoint;
	}

	private static LineString unpack(Proto.LineString proto) {
		var lineString = new LineString();
		for (int i = 0; i < proto.getPointCount(); i++) {
			var point = unpack(proto.getPoint(i));
			lineString.points.add(point);
		}
		return lineString;
	}

	private static MultiLineString unpack(Proto.MultiLineString proto) {
		var multiLineString = new MultiLineString();
		for (int i = 0; i < proto.getLineStringCount(); i++) {
			var lineString = unpack(proto.getLineString(i));
			multiLineString.lineStrings.add(lineString);
		}
		return multiLineString;
	}

	private static Polygon unpack(Proto.Polygon proto) {
		var polygon = new Polygon();
		for (int i = 0; i < proto.getRingCount(); i++) {
			var ring = unpack(proto.getRing(i));
			polygon.rings.add(ring);
		}
		return polygon;
	}

	private static MultiPolygon unpack(Proto.MultiPolygon proto) {
		var multiPolygon = new MultiPolygon();
		for (int i = 0; i < proto.getPolygonCount(); i++) {
			var polygon = unpack(proto.getPolygon(i));
			multiPolygon.polygons.add(polygon);
		}
		return multiPolygon;
	}

	private static GeometryCollection unpack(Proto.GeometryCollection proto) {
		var coll = new GeometryCollection();
		for (int i = 0; i < proto.getGeometryCount(); i++) {
			var geometry = unpack(proto.getGeometry(i));
			coll.geometries.add(geometry);
		}
		return coll;
	}
}
