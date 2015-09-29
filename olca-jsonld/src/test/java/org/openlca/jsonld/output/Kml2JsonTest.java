package org.openlca.jsonld.output;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Kml2JsonTest {

	private final boolean DEBUG = true;

	@Test
	public void testPoint() {
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">"
				+ "  <Folder>"
				+ "    <name>OpenLayers export</name>"
				+ "    <description>Exported on Wed Sep 30 2015 11:56:19 GMT+0200</description>"
				+ "    <Placemark>"
				+ "      <name>OpenLayers_Feature_Vector_198</name>"
				+ "      <description>No description available</description>"
				+ "      <Point>"
				+ "        <coordinates>35.85,55.87</coordinates>"
				+ "      </Point>"
				+ "    </Placemark>"
				+ "  </Folder>"
				+ "</kml>";
		JsonObject obj = Kml2Json.read(kml);
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
				+ "    <name>OpenLayers export</name>"
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
		JsonObject obj = Kml2Json.read(kml);
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
				+ "    <name>OpenLayers export</name>"
				+ "    <description>Exported on Wed Sep 30 2015 15:07:47 GMT+0200</description>"
				+ "    <Placemark>"
				+ "      <name>OpenLayers_Feature_Vector_185</name>"
				+ "      <description>No description available</description>"
				+ "      <Polygon>"
				+ "        <outerBoundaryIs>"
				+ "          <LinearRing>"
				+ "            <coordinates>"
				+ "                35.8435231447225,55.87565691696789 "
				+ "                35.8602601289755,55.8719973275629 "
				+ "                35.85287868976608,55.86756683681068 "
				+ "                35.83545506000593,55.86881898325944 "
				+ "                35.834940075875124,55.8732974843514 "
				+ "                35.8435231447225,55.87565691696789"
				+ "            </coordinates>"
				+ "          </LinearRing>"
				+ "        </outerBoundaryIs>"
				+ "      </Polygon>"
				+ "    </Placemark>"
				+ "  </Folder>"
				+ "</kml>";
		JsonObject obj = Kml2Json.read(kml);
		Assert.assertEquals("Polygon", obj.get("type").getAsString());
		// JsonArray coordinates = obj.get("coordinates").getAsJsonArray();
		// JsonArray thirdPoint =
		// coordinates.get(0).getAsJsonArray().get(0).getAsJsonArray();
		// Assert.assertEquals(35.83, lastPoint.get(0).getAsDouble(), 1e-8);
		// Assert.assertEquals(55.86, lastPoint.get(1).getAsDouble(), 1e-8);
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
