package org.openlca.geo.geojson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class GeoJSONTest {

	@Test
	public void testGeometryTypes() {
		List<Class<? extends Geometry>> geomTypes = Arrays.asList(
				GeometryCollection.class,
				LineString.class,
				MultiLineString.class,
				MultiPoint.class,
				MultiPolygon.class,
				Point.class,
				Polygon.class);
		geomTypes.forEach(gt -> {
			try {

				// check the type of the serialized JSON object
				Geometry geom = gt.newInstance();
				JsonObject obj = geom.toJson();
				String type = obj.get("type").getAsString();
				Assert.assertEquals(gt.getSimpleName(), type);

				// test basic de-/serialization
				StringWriter w = new StringWriter();
				GeoJSON.write(geom, w);
				StringReader r = new StringReader(w.toString());
				FeatureCollection coll = GeoJSON.read(r);
				Assert.assertEquals(1, coll.features.size());
				Assert.assertEquals(gt,
						coll.features.get(0).geometry.getClass());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	public void testPoint() {
		Consumer<Point> check = (p) -> {
			Assert.assertEquals(13.28, p.x, 1e-10);
			Assert.assertEquals(52.51, p.y, 1e-10);
		};
		Point point = (Point) readGeometry("point.geojson");
		check.accept(point);
		check.accept(writeRead(point));
	}

	@Test
	public void testLineString() {
		Consumer<LineString> check = (line) -> {
			Assert.assertEquals(3, line.points.size());
			double[][] points = {
					{13.26, 52.52},
					{13.27, 52.51},
					{13.28, 52.51},
			};
			for (int i = 0; i < points.length; i++) {
				Assert.assertEquals(
						points[i][0], line.points.get(i).x, 1e-10);
				Assert.assertEquals(
						points[i][1], line.points.get(i).y, 1e-10);
			}
		};
		LineString line = (LineString) readGeometry(
				"linestring.geojson");
		check.accept(line);
		check.accept(writeRead(line));
	}

	@Test
	public void testPolygon() {
		Consumer<Polygon> check = (polygon) -> {
			LineString ext = polygon.rings.get(0);
			Assert.assertEquals(4, ext.points.size());
			double[][] points = {
					{13.30, 52.52},
					{13.25, 52.50},
					{13.29, 52.51},
					{13.30, 52.52},
			};
			for (int i = 0; i < points.length; i++) {
				Assert.assertEquals(
						points[i][0], ext.points.get(i).x, 1e-10);
				Assert.assertEquals(
						points[i][1], ext.points.get(i).y, 1e-10);
			}
		};
		Polygon polygon = (Polygon) readGeometry(
				"polygon.geojson");
		check.accept(polygon);
		check.accept(writeRead(polygon));
	}

	private Geometry readGeometry(String file) {
		try (InputStream stream = getClass().getResourceAsStream(file);
			 InputStreamReader reader = new InputStreamReader(
					 stream, StandardCharsets.UTF_8);
			 BufferedReader buffer = new BufferedReader(reader)) {
			FeatureCollection coll = GeoJSON.read(buffer);
			return coll.features.get(0).geometry;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	static <T extends Geometry> T writeRead(T geometry) {
		try {
			File file = Files.createTempFile(
					"_olca_geojson_", ".geojson").toFile();
			GeoJSON.write(geometry, file);
			FeatureCollection coll = GeoJSON.read(file);
			Assert.assertTrue(file.delete());
			return (T) coll.features.get(0).geometry;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
