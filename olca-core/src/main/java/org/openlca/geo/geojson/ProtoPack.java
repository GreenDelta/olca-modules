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

		}
		return proto.build();
	}
}
