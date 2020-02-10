package org.openlca.geo.geojson;

import java.io.IOException;
import java.util.Map;

import org.geotools.util.MapEntry;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

public class MsgPack {

	private MsgPack() {
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

	}


	static void packPoint(Point p, MessagePacker packer) {
		try {
			packer.packString("type");
			packer.packString("Point");
			packer.packString("coordinates");
			packer.packArrayHeader(2);
			packer.packDouble(p.x);
			packer.packDouble(p.y);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
				return unpackPoint(map);
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

	private static Point unpackPoint(MapValue value) {
		if (value == null)
			return null;
		Point point = new Point();
		return point;
	}

	private static MultiPoint unpackMultiPoint(MapValue value) {
		if (value == null)
			return null;
		MultiPoint g = new MultiPoint();
		return g;
	}

	private static LineString unpackLineString(MapValue value) {
		if (value == null)
			return null;
		LineString g = new LineString();
		return g;
	}

	private static MultiLineString unpackMultiLineString(MapValue value) {
		if (value == null)
			return null;
		MultiLineString g = new MultiLineString();
		return g;
	}

	private static Polygon unpackPolygon(MapValue value) {
		if (value == null)
			return null;
		Polygon g = new Polygon();
		return g;
	}

	private static MultiPolygon unpackMultiPolygon(MapValue value) {
		if (value == null)
			return null;
		MultiPolygon g = new MultiPolygon();
		return g;
	}

	private static GeometryCollection unpackGeometryCollection(MapValue value) {
		if (value == null)
			return null;
		GeometryCollection g = new GeometryCollection();
		return g;
	}

	private static ArrayValue unpackCoordinates(Value geometry) {
		return null;
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

	static Point unpackPoint(MessageUnpacker unpacker) {
		Point p = new Point();
		try {
			System.out.println(unpacker.unpackString());
			System.out.println(unpacker.unpackString());

			System.out.println(unpacker.unpackString()); // "coordinates"
			int n = unpacker.unpackArrayHeader();
			if (n > 0) {
				p.x = unpacker.unpackDouble();
			}
			if (n > 1) {
				p.y = unpacker.unpackDouble();
			}
			if (n > 2) {
				for (int i = 2; i < n; i++) {
					unpacker.unpackDouble();
				}
			}
			return p;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
