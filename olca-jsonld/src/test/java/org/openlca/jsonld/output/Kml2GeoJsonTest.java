package org.openlca.jsonld.output;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Kml2GeoJsonTest {

	private final boolean DEBUG = false;

	@Test
	public void testPoint() {
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">"
				+ "  <Folder>"
				+ "    <Placemark>"
				+ "      <name>OpenLayers_Feature_Vector_198</name>"
				+ "      <description>No description available</description>"
				+ "      <Point>"
				+ "        <coordinates>35.85,55.87</coordinates>"
				+ "      </Point>"
				+ "    </Placemark>"
				+ "  </Folder>"
				+ "</kml>";
		JsonObject obj = Kml2GeoJson.convert(kml);
		Assert.assertEquals("Point", obj.get("type").getAsString());
		JsonArray coordinates = obj.get("coordinates").getAsJsonArray();
		Assert.assertEquals(35.85, coordinates.get(0).getAsDouble(), 1e-8);
		Assert.assertEquals(55.87, coordinates.get(1).getAsDouble(), 1e-8);
		print(kml, obj);
	}

	@Test
	public void testLineString() {
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">"
				+ "  <Folder>"
				+ "    <Placemark>"
				+ "      <name>OpenLayers_Feature_Vector_204</name>"
				+ "      <description>No description available</description>"
				+ "      <LineString>"
				+ "        <coordinates>35.84,55.88 35.85,55.87 35.84,55.87 "
				+ "                     35.84,55.86 35.83,55.86</coordinates>"
				+ "      </LineString>"
				+ "    </Placemark>"
				+ "  </Folder>"
				+ "</kml>";
		JsonObject obj = Kml2GeoJson.convert(kml);
		Assert.assertEquals("LineString", obj.get("type").getAsString());
		JsonArray coordinates = obj.get("coordinates").getAsJsonArray();
		JsonArray lastPoint = coordinates.get(4).getAsJsonArray();
		Assert.assertEquals(35.83, lastPoint.get(0).getAsDouble(), 1e-8);
		Assert.assertEquals(55.86, lastPoint.get(1).getAsDouble(), 1e-8);
		print(kml, obj);
	}

	@Test
	public void testPolygon() {
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">"
				+ "  <Folder>"
				+ "    <Placemark>"
				+ "      <name>OpenLayers_Feature_Vector_185</name>"
				+ "      <description>No description available</description>"
				+ "      <Polygon>"
				+ "        <outerBoundaryIs>"
				+ "          <LinearRing>"
				+ "            <coordinates>"
				+ "                35.8435,55.8756"
				+ "                35.8602,55.8719"
				+ "                35.8528,55.8675"
				+ "                35.8354,55.8688"
				+ "                35.8349,55.8732"
				+ "                35.8435,55.8756"
				+ "            </coordinates>"
				+ "          </LinearRing>"
				+ "        </outerBoundaryIs>"
				+ "      </Polygon>"
				+ "    </Placemark>"
				+ "  </Folder>"
				+ "</kml>";
		JsonObject obj = Kml2GeoJson.convert(kml);
		Assert.assertEquals("Polygon", obj.get("type").getAsString());
		JsonArray coordinates = obj.get("coordinates").getAsJsonArray();
		JsonArray outerRing = coordinates.get(0).getAsJsonArray();
		JsonArray thirdPoint = outerRing.get(2).getAsJsonArray();
		Assert.assertEquals(35.8528, thirdPoint.get(0).getAsDouble(), 1e-8);
		Assert.assertEquals(55.8675, thirdPoint.get(1).getAsDouble(), 1e-8);
		print(kml, obj);
	}

	@Test
	public void testMultiGreometry() {
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">"
				+ "<Placemark>"
				+ "  <name>SF Marina Harbor Master</name>"
				+ "  <visibility>0</visibility>"
				+ "  <MultiGeometry>"
				+ "    <LineString>"
				+ "      <coordinates>"
				+ "        -122.4425,37.8066,0"
				+ "        -122.4428,37.8066,0"
				+ "      </coordinates>"
				+ "    </LineString>"
				+ "    <LineString>"
				+ "      <coordinates>"
				+ "        -122.4425,37.8066,0"
				+ "        -122.4428,37.8065,0"
				+ "      </coordinates>"
				+ "    </LineString>"
				+ "  </MultiGeometry>"
				+ "</Placemark>"
				+ "</kml>";
		JsonObject obj = Kml2GeoJson.convert(kml);
		Assert.assertEquals("GeometryCollection", obj.get("type").getAsString());
		JsonArray geometries = obj.get("geometries").getAsJsonArray();
		JsonObject second = geometries.get(1).getAsJsonObject();
		Assert.assertEquals("LineString", second.get("type").getAsString());
		JsonArray point = second.get("coordinates").getAsJsonArray().get(1)
				.getAsJsonArray();
		Assert.assertEquals(-122.4428, point.get(0).getAsDouble(), 1e-8);
		Assert.assertEquals(37.8065, point.get(1).getAsDouble(), 1e-8);
		print(kml, obj);
	}

	private void print(String kml, JsonObject obj) {
		if (!DEBUG)
			return;
		System.out.println("converted KML = ");
		System.out.println(kml);
		System.out.println("to GeoJSON = ");
		String json = new GsonBuilder().setPrettyPrinting()
				.create().toJson(obj);
		System.out.println(json);
	}

}
