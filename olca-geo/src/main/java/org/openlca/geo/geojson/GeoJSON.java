package org.openlca.geo.geojson;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

	public static FeatureCollection read(File file) {
		try (Reader r = Files.newBufferedReader(
				file.toPath(), StandardCharsets.UTF_8)) {
			return read(r);
		} catch (Exception e) {
			throw new RuntimeException("failed to read " + file, e);
		}
	}

	public static FeatureCollection fromKryo(byte[] data) {
		if (data == null)
			return null;
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		Input input = new Input(data);
		Object obj = kryo.readClassAndObject(input);
		return obj instanceof FeatureCollection
				? (FeatureCollection) obj
				: null;
	}

	public static byte[] toKryo(FeatureCollection coll) {
		if (coll == null)
			return null;
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		Output output = new Output(1024, -1);
		kryo.writeClassAndObject(output, coll);
		output.close();
		return output.getBuffer();
	}

	public static FeatureCollection read(Reader reader) {
		Gson gson = new Gson();
		JsonObject obj = gson.fromJson(reader, JsonObject.class);
		JsonElement typeElem = obj.get("type");
		if (typeElem == null || !typeElem.isJsonPrimitive())
			throw new IllegalStateException("no valid type element found");
		String type = typeElem.getAsString();
		switch (type) {
			case "FeatureCollection":
				return FeatureCollection.fromJson(obj);
			case "Feature":
				return FeatureCollection.of(Feature.fromJson(obj));
			default:
				Geometry geom = readGeometry(obj);
				if (geom == null)
					throw new IllegalStateException(
							"unknown Geometry type: " + type);
				return FeatureCollection.of(geom);
		}
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

	static Geometry readGeometry(JsonObject obj) {
		if (obj == null)
			return null;
		JsonElement typeElem = obj.get("type");
		if (typeElem == null || !typeElem.isJsonPrimitive())
			return null;
		String type = typeElem.getAsString();
		switch (type) {
			case "Point":
				return Point.fromJson(obj);
			case "MultiPoint":
				return MultiPoint.fromJson(obj);
			case "LineString":
				return LineString.fromJson(obj);
			case "MultiLineString":
				return MultiLineString.fromJson(obj);
			case "Polygon":
				return Polygon.fromJson(obj);
			case "MultiPolygon":
				return MultiPolygon.fromJson(obj);
			case "GeometryCollection":
				return GeometryCollection.fromJson(obj);
			default:
				return null;
		}
	}
}
