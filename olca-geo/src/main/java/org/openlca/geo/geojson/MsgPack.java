package org.openlca.geo.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.openlca.util.BinUtils;

public class MsgPack {

	private MsgPack() {
	}

	/**
	 * Converts the given feature collection into the message pack format and
	 * compresses it with gzip. This is the format that we use for storing
	 * geographic data of locations in an openLCA database.
	 */
	public static byte[] packgz(FeatureCollection coll) {
		if (coll == null)
			return null;
		try {
			byte[] data = MsgPack.pack(coll);
			return BinUtils.gzip(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * First, decompresses the given data using gzip and then parses the result as
	 * message pack format.
	 */
	public static FeatureCollection unpackgz(byte[] data) {
		if (data == null || data.length == 0)
			return null;
		try {
			byte[] raw = BinUtils.gunzip(data);
			return MsgPack.unpack(raw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] pack(FeatureCollection coll) {
		if (coll == null)
			return null;
		try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
			packer.packMapHeader(2);
			packer.packString("type");
			packer.packString("FeatureCollection");
			packer.packString("features");
			packer.packArrayHeader(coll.features.size());
			for (Feature f : coll.features) {
				packFeature(f, packer);
			}
			return packer.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void packFeature(
			Feature f, MessagePacker packer) throws IOException {
		if (f == null) {
			packer.packNil();
			return;
		}
		// TODO: we currently do not support properties
		packer.packMapHeader(2);
		packer.packString("type");
		packer.packString("Feature");
		packer.packString("geometry");
		packGeometry(f.geometry, packer);
	}

	private static void packGeometry(
			Geometry g, MessagePacker packer) throws IOException {
		if (g == null) {
			packer.packNil();
			return;
		}

		packer.packMapHeader(2);
		packer.packString("type");

		if (g instanceof Point) {
			packer.packString("Point");
			packer.packString("coordinates");
			packPoint((Point) g, packer);
			return;
		}

		if (g instanceof MultiPoint) {
			packer.packString("MultiPoint");
			packer.packString("coordinates");
			packPoints(((MultiPoint) g).points, packer);
			return;
		}

		if (g instanceof LineString) {
			packer.packString("LineString");
			packer.packString("coordinates");
			packPoints(((LineString) g).points, packer);
			return;
		}

		if (g instanceof MultiLineString) {
			packer.packString("MultiLineString");
			packer.packString("coordinates");
			packLines(((MultiLineString) g).lineStrings, packer);
			return;
		}

		if (g instanceof Polygon) {
			packer.packString("Polygon");
			packer.packString("coordinates");
			packLines(((Polygon) g).rings, packer);
			return;
		}

		if (g instanceof MultiPolygon) {
			packer.packString("MultiPolygon");
			packer.packString("coordinates");
			List<Polygon> polygons = ((MultiPolygon) g).polygons;
			packer.packArrayHeader(polygons.size());
			for (Polygon p : polygons) {
				packLines(p.rings, packer);
			}
			return;
		}

		if (g instanceof GeometryCollection) {
			packer.packString("GeometryCollection");
			packer.packString("geometries");
			GeometryCollection coll = (GeometryCollection) g;
			packer.packArrayHeader(coll.geometries.size());
			for (Geometry gg : coll.geometries) {
				packGeometry(gg, packer);
			}
			return;
		}

		packer.packString("UNKNOWN");
		packer.packString("coordinates");
		packer.packNil();
	}

	private static void packPoint(
			Point point, MessagePacker packer) throws IOException {
		if (point == null) {
			packer.packNil();
			return;
		}
		packer.packArrayHeader(2);
		packer.packDouble(point.x);
		packer.packDouble(point.y);
	}

	private static void packPoints(
			List<Point> points, MessagePacker packer) throws IOException {
		packer.packArrayHeader(points.size());
		for (Point p : points) {
			packPoint(p, packer);
		}
	}

	private static void packLines(
			List<LineString> lines, MessagePacker packer) throws IOException {
		packer.packArrayHeader(lines.size());
		for (LineString line : lines) {
			packPoints(line.points, packer);
		}
	}

	public static FeatureCollection unpack(byte[] data) {
		if (data == null)
			return null;
		try {
			MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
			FeatureCollection coll = new FeatureCollection();
			if (!unpacker.hasNext())
				return coll;
			Value root = unpacker.unpackValue();
			if (!root.isMapValue())
				return coll;
			Value features = getField("features", root.asMapValue());
			if (features == null || !features.isArrayValue())
				return coll;
			ArrayValue array = features.asArrayValue();
			for (int i = 0; i < array.size(); i++) {
				Value featureVal = array.get(i);
				if (featureVal == null || !featureVal.isMapValue())
					continue;
				Feature feature = new Feature();
				feature.geometry = unpackGeometry(
						getField("geometry", featureVal.asMapValue()));
				coll.features.add(feature);
			}
			return coll;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Geometry unpackGeometry(Value value) {
		if (value == null || !value.isMapValue())
			return null;
		MapValue map = value.asMapValue();
		Value typeVal = getField("type", map);
		if (typeVal == null || !typeVal.isStringValue())
			return null;
		switch (typeVal.toString()) {
			case "Point":
				return unpackPoint(unpackCoordinates(map));
			case "MultiPoint":
				return unpackMultiPoint(map);
			case "LineString":
				return unpackLineString(map);
			case "MultiLineString":
				return unpackMultiLineString(map);
			case "Polygon":
				return unpackPolygon(map);
			case "MultiPolygon":
				return unpackMultiPolygon(map);
			case "GeometryCollection":
				return unpackGeometryCollection(map);
			default:
				return null;
		}
	}

	private static Point unpackPoint(ArrayValue coordinates) {
		if (coordinates == null || coordinates.size() < 2)
			return null;
		Point p = new Point();
		Value xVal = coordinates.get(0);
		if (xVal != null && xVal.isNumberValue()) {
			p.x = xVal.asNumberValue().toDouble();
		}
		Value yVal = coordinates.get(1);
		if (yVal != null && yVal.isNumberValue()) {
			p.y = yVal.asNumberValue().toDouble();
		}
		return p;
	}

	private static List<Point> unpackPoints(ArrayValue coordinates) {
		if (coordinates == null)
			return Collections.emptyList();
		List<Point> points = new ArrayList<>(coordinates.size());
		for (int i = 0; i < coordinates.size(); i++) {
			Value vi = coordinates.get(i);
			if (vi == null || !vi.isArrayValue())
				continue;
			Point p = unpackPoint(vi.asArrayValue());
			if (p != null) {
				points.add(p);
			}
		}
		return points;
	}

	private static MultiPoint unpackMultiPoint(MapValue value) {
		ArrayValue coordinates = unpackCoordinates(value);
		if (coordinates == null)
			return null;
		List<Point> points = unpackPoints(coordinates);
		return new MultiPoint(points);
	}

	private static LineString unpackLineString(MapValue value) {
		ArrayValue coordinates = unpackCoordinates(value);
		if (coordinates == null)
			return null;
		List<Point> points = unpackPoints(coordinates);
		return new LineString(points);
	}

	private static List<LineString> unpackLineStrings(ArrayValue coordinates) {
		if (coordinates == null)
			return Collections.emptyList();
		List<LineString> lines = new ArrayList<>(coordinates.size());
		for (int i = 0; i < coordinates.size(); i++) {
			Value vi = coordinates.get(i);
			if (vi == null || !vi.isArrayValue())
				continue;
			List<Point> points = unpackPoints(vi.asArrayValue());
			lines.add(new LineString(points));
		}
		return lines;
	}

	private static MultiLineString unpackMultiLineString(MapValue value) {
		ArrayValue coordinates = unpackCoordinates(value);
		if (coordinates == null)
			return null;
		List<LineString> lines = unpackLineStrings(coordinates);
		return new MultiLineString(lines);
	}

	private static Polygon unpackPolygon(MapValue value) {
		ArrayValue coordinates = unpackCoordinates(value);
		if (coordinates == null)
			return null;
		List<LineString> rings = unpackLineStrings(coordinates);
		return new Polygon(rings);
	}

	private static MultiPolygon unpackMultiPolygon(MapValue value) {
		ArrayValue coordinates = unpackCoordinates(value);
		if (coordinates == null)
			return null;
		List<Polygon> polygons = new ArrayList<>(coordinates.size());
		for (int i = 0; i < coordinates.size(); i++) {
			Value vi = coordinates.get(i);
			if (vi == null || !vi.isArrayValue())
				continue;
			List<LineString> rings = unpackLineStrings(vi.asArrayValue());
			if (rings.size() > 0) {
				polygons.add(new Polygon(rings));
			}
		}
		return new MultiPolygon(polygons);
	}

	private static GeometryCollection unpackGeometryCollection(MapValue map) {
		if (map == null)
			return null;
		GeometryCollection coll = new GeometryCollection();
		Value geomsVal = getField("geometries", map);
		if (geomsVal == null || !geomsVal.isArrayValue())
			return coll;
		ArrayValue geometries = geomsVal.asArrayValue();
		for (int i = 0; i < geometries.size(); i++) {
			Geometry g = unpackGeometry(geometries.get(i));
			if (g != null) {
				coll.geometries.add(g);
			}
		}
		return coll;
	}

	private static ArrayValue unpackCoordinates(MapValue geometry) {
		Value v = getField("coordinates", geometry);
		if (v == null || !v.isArrayValue())
			return null;
		return v.asArrayValue();
	}

	private static Value getField(String key, MapValue value) {
		if (value == null)
			return null;
		for (Map.Entry<Value, Value> e : value.entrySet()) {
			Value keyVal = e.getKey();
			if (!keyVal.isStringValue())
				continue;
			if (key.equals(keyVal.toString()))
				return e.getValue();
		}
		return null;
	}

}
