package org.openlca.jsonld.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GeoJson2KmlTest {

	private final boolean DEBUG = true;

	@Test
	public void testEmpty() {
		JsonObject empty = new JsonObject();
		String kml = GeoJson2Kml.convert(empty);
		print(empty, kml);
		check(kml, "kml", "Folder");
	}

	@Test
	public void testPoint() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "Point");
		JsonArray coordinates = new JsonArray();
		coordinates.add(new JsonPrimitive(100.1));
		coordinates.add(new JsonPrimitive(50.1));
		coordinates.add(new JsonPrimitive(10.1));
		obj.add("coordinates", coordinates);
		String kml = GeoJson2Kml.convert(obj);
		print(obj, kml);
		check(kml, "Point", "coordinates", "100.1,50.1,10.1");
	}

	@Test
	public void testLineString() {
		String json = "{ "
				+ "  \"type\": \"LineString\","
				+ "  \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]"
				+ "}";
		JsonObject obj = new Gson().fromJson(json, JsonObject.class);
		String kml = GeoJson2Kml.convert(obj);
		print(obj, kml);
		check(kml, "LineString", "coordinates", "100.0,0.0", "101.0,1.0");
	}

	@Test
	public void testPolygon() {
		String json = "{ \"type\": \"Polygon\","
				+ "  \"coordinates\": ["
				+ "      [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ],"
				+ "      [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]"
				+ "  ]"
				+ "}";
		JsonObject obj = new Gson().fromJson(json, JsonObject.class);
		String kml = GeoJson2Kml.convert(obj);
		print(obj, kml);
		check(kml, "Polygon", "outerBoundaryIs", "LinearRing", "coordinates",
				"100.0,0.0", "innerBoundaryIs", "LinearRing", "coordinates",
				"100.2,0.2");
	}

	@Test
	public void testGeometryCollection() {
		String json = "{ \"type\": \"GeometryCollection\","
				+ "  \"geometries\": ["
				+ "    { \"type\": \"Point\","
				+ "      \"coordinates\": [100.0, 0.0]"
				+ "    },"
				+ "    { \"type\": \"LineString\","
				+ "      \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]"
				+ "    }"
				+ "  ]"
				+ "}";
		JsonObject obj = new Gson().fromJson(json, JsonObject.class);
		String kml = GeoJson2Kml.convert(obj);
		print(obj, kml);
		check(kml, "MultiGeometry", "Point", " 100.0,0.0", "LineString",
				"102.0,1.0");
	}

	private void check(String kml, String... parts) {
		String any = "(.*)";
		String regex = any;
		for (String part : parts)
			regex += part + any;
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(kml);
		Assert.assertTrue(matcher.matches());
	}

	private void print(JsonObject obj, String kml) {
		if (!DEBUG)
			return;
		System.out.println("Converted GeoJSON: ");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(obj));
		System.out.println("to KML: ");
		System.out.println(kml);
		System.out.println();
	}

}
