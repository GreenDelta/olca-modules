package org.openlca.geo.geojson;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
			case "Point":
				return FeatureCollection.of(Point.fromJson(obj));
			case "MultiPoint":
				return FeatureCollection.of(MultiPoint.fromJson(obj));
			case "LineString":
				return FeatureCollection.of(LineString.fromJson(obj));
			case "MultiLineString":
				return FeatureCollection.of(MultiLineString.fromJson(obj));
			default:
				throw new IllegalStateException(
						"unknown GeoJSON type: " + type);
		}
	}

	public static void write(FeatureCollection coll, File file) {
		try (Writer w = Files.newBufferedWriter(
				file.toPath(), StandardCharsets.UTF_8)) {
			write(coll, w);
		} catch (Exception e) {
			throw new RuntimeException("failed to write " + file, e);
		}
	}

	public static void write(Feature feature, File file) {
		try (Writer w = Files.newBufferedWriter(
				file.toPath(), StandardCharsets.UTF_8)) {
			write(feature, w);
		} catch (Exception e) {
			throw new RuntimeException("failed to write " + file, e);
		}
	}

	public static void write(Geometry geometry, File file) {
		try (Writer w = Files.newBufferedWriter(
				file.toPath(), StandardCharsets.UTF_8)) {
			write(geometry, w);
		} catch (Exception e) {
			throw new RuntimeException("failed to write " + file, e);
		}
	}

	public static void write(FeatureCollection coll, Writer writer) {
		// TODO
	}

	public static void write(Feature feature, Writer writer) {
		// TODO
	}

	public static void write(Geometry geometry, Writer writer) {
		if (geometry == null)
			return;
		JsonObject obj = geometry.toJson();
		new Gson().toJson(obj, writer);
	}
}
