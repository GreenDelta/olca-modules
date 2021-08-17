package org.openlca.geo.geojson;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.openlca.util.BinUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The entry point for reading and writing GeoJSON objects from and to GeoJSON
 * text. GeoJSON text can contain a single object of type FeatureCollection,
 * Feature, or Geometry. When reading a GeoJSON object from GeoJSON text, we
 * always wrap the content into a FeatureCollection (e.g. if it contains a
 * single geometry we wrap it into a feature collection where the geometry of
 * the only feature in this collection is that geometry).
 */
public final class GeoJSON {

	private GeoJSON() {
	}

	/**
	 * Converts the given feature collection into the openLCA Protocol Buffers
	 * format for GeoJSON data and compresses it with gzip. This is the format that
	 * we use for storing geographic data of locations in an openLCA database.
	 */
	public static byte[] pack(FeatureCollection coll) {
		return ProtoPack.packgz(coll);
	}

	/**
	 * Extracts a feature collection from a byte array that contains data in the
	 * openLCA Protocol Buffers format for GeoJSON.
	 */
	public static FeatureCollection unpack(byte[] data) {
		return BinUtils.isGzip(data)
			? ProtoPack.unpackgz(data)
			: ProtoPack.unpack(data);
	}

	public static FeatureCollection read(File file) {
		try (Reader r = Files.newBufferedReader(
			file.toPath(), StandardCharsets.UTF_8)) {
			return read(r);
		} catch (Exception e) {
			throw new RuntimeException("failed to read " + file, e);
		}
	}

	public static FeatureCollection read(Reader reader) {
		Gson gson = new Gson();
		JsonObject obj = gson.fromJson(reader, JsonObject.class);
		return read(obj);
	}

	public static FeatureCollection read(JsonObject obj) {
		if (obj == null)
			return FeatureCollection.empty();
		var type = obj.get("type");
		if (type == null || !type.isJsonPrimitive())
			return FeatureCollection.empty();

		switch (type.getAsString()) {
			case "FeatureCollection":
				return FeatureCollection.fromJson(obj);
			case "Feature":
				return FeatureCollection.of(Feature.fromJson(obj));
			default:
				var geom = readGeometry(obj);
				return geom == null
					? FeatureCollection.empty()
					: FeatureCollection.of(geom);
		}
	}

	static Geometry readGeometry(JsonObject obj) {
		if (obj == null)
			return null;
		var type = obj.get("type");
		if (type == null || !type.isJsonPrimitive())
			return null;
		return switch (type.getAsString()) {
			case "Point" -> Point.fromJson(obj);
			case "MultiPoint" -> MultiPoint.fromJson(obj);
			case "LineString" -> LineString.fromJson(obj);
			case "MultiLineString" -> MultiLineString.fromJson(obj);
			case "Polygon" -> Polygon.fromJson(obj);
			case "MultiPolygon" -> MultiPolygon.fromJson(obj);
			case "GeometryCollection" -> GeometryCollection.fromJson(obj).trySimplify();
			default -> null;
		};
	}

	public static void write(FeatureCollection coll, File file) {
		if (coll == null || file == null)
			return;
		write(coll.toJson(), file);
	}

	public static void write(Feature feature, File file) {
		if (feature == null || file == null)
			return;
		write(feature.toJson(), file);
	}

	public static void write(Geometry geometry, File file) {
		if (geometry == null || file == null)
			return;
		write(geometry.toJson(), file);
	}

	private static void write(JsonObject obj, File file) {
		if (obj == null || file == null)
			return;
		try (Writer w = Files.newBufferedWriter(
			file.toPath(), StandardCharsets.UTF_8)) {
			write(obj, w);
		} catch (Exception e) {
			throw new RuntimeException("failed to write " + file, e);
		}
	}

	public static void write(FeatureCollection coll, Writer writer) {
		if (coll == null || writer == null)
			return;
		write(coll.toJson(), writer);
	}

	public static void write(Feature feature, Writer writer) {
		if (feature == null || writer == null)
			return;
		write(feature.toJson(), writer);
	}

	public static void write(Geometry geometry, Writer writer) {
		if (geometry == null)
			return;
		write(geometry.toJson(), writer);
	}

	private static void write(JsonObject obj, Writer writer) {
		if (obj == null || writer == null)
			return;
		new Gson().toJson(obj, writer);
	}
}
