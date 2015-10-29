package org.openlca.jsonld.input;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A minimal converter of GeoJSON to KML (as used in openLCA).
 * 
 * @see https://developers.google.com/kml/documentation/kmlreference
 * @see http://geojson.org/geojson-spec.html
 */
class GeoJson2Kml {

	private XMLStreamWriter kml;

	private int indent = 0;

	private GeoJson2Kml(XMLStreamWriter kml) {
		this.kml = kml;
	}

	static String convert(JsonObject geoJson) {
		if (geoJson == null)
			return null;
		XMLOutputFactory fac = XMLOutputFactory.newInstance();
		try (StringWriter writer = new StringWriter()) {
			XMLStreamWriter kml = fac.createXMLStreamWriter(writer);
			new GeoJson2Kml(kml).doIt(geoJson);
			kml.flush();
			writer.flush();
			kml.close();
			return writer.toString();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(GeoJson2Kml.class);
			log.error("failed to convert GeoJSON", e);
			return null;
		}
	}

	private void doIt(JsonObject geoJson) throws Exception {
		kml.writeStartDocument("utf-8", "1.0");
		kml.setDefaultNamespace("http://earth.google.com/kml/2.0");
		startElem("kml");
		kml.writeNamespace("", "http://earth.google.com/kml/2.0");
		startElems("Folder", "Placemark");
		writeGeometry(geoJson);
		endElems(3);
		kml.writeEndDocument();
	}

	private void writeGeometry(JsonObject geoJson) throws Exception {
		JsonElement typeElem = geoJson.get("type");
		if (typeElem == null || !typeElem.isJsonPrimitive())
			return;
		String type = typeElem.getAsString();
		switch (type) {
		case "Point":
			writePoint(geoJson);
			break;
		case "LineString":
			writeLineString(geoJson);
			break;
		case "Polygon":
			writePolygon(geoJson);
			break;
		case "GeometryCollection":
			writeGeometryCollection(geoJson);
			break;
		}

	}

	private void writePoint(JsonObject geoJson) throws Exception {
		String coordinate = getCoordinate(geoJson.get("coordinates"));
		if (coordinate == null)
			return;
		startElems("Point", "coordinates");
		writeCoordinate(coordinate);
		endElem();
		endElem();
	}

	private void writeLineString(JsonObject geoJson) throws Exception {
		JsonElement elem = geoJson.get("coordinates");
		List<String> coordinates = getCoordinates(elem);
		if (coordinates.isEmpty())
			return;
		startElems("LineString", "coordinates");
		for (String coordinate : coordinates)
			writeCoordinate(coordinate);
		endElems(2);
	}

	private void writePolygon(JsonObject geoJson) throws Exception {
		JsonElement elem = geoJson.get("coordinates");
		if (elem == null || !elem.isJsonArray())
			return;
		JsonArray array = elem.getAsJsonArray();
		if (array.size() == 0)
			return;
		List<String> outerRing = getCoordinates(array.get(0));
		if (outerRing.isEmpty())
			return;
		startElem("Polygon");
		startElems("outerBoundaryIs", "LinearRing", "coordinates");
		for (String coordinate : outerRing)
			writeCoordinate(coordinate);
		endElems(3);
		if (array.size() > 1) {
			List<String> innerRing = getCoordinates(array.get(1));
			startElems("innerBoundaryIs", "LinearRing", "coordinates");
			for (String coordinate : innerRing)
				writeCoordinate(coordinate);
			endElems(3);
		}
		endElem();
	}

	private void writeGeometryCollection(JsonObject geoJson) throws Exception {
		JsonElement elem = geoJson.get("geometries");
		if (elem == null || !elem.isJsonArray())
			return;
		startElem("MultiGeometry");
		for (JsonElement geom : elem.getAsJsonArray()) {
			if (!geom.isJsonObject())
				continue;
			writeGeometry(geom.getAsJsonObject());
		}
		endElem();
	}

	private void writeCoordinate(String coordinate) throws XMLStreamException {
		kml.writeCharacters("\n");
		for (int i = 0; i < indent; i++)
			kml.writeCharacters("  ");
		kml.writeCharacters(coordinate);
	}

	private List<String> getCoordinates(JsonElement elem) {
		if (elem == null || !elem.isJsonArray())
			return Collections.emptyList();
		JsonArray array = elem.getAsJsonArray();
		List<String> coordinates = new ArrayList<>();
		for (JsonElement ce : array) {
			String coordinate = getCoordinate(ce);
			if (coordinate == null)
				return Collections.emptyList();
			coordinates.add(coordinate);
		}
		return coordinates;
	}

	private String getCoordinate(JsonElement elem) {
		if (elem == null || !elem.isJsonArray())
			return null;
		JsonArray point = elem.getAsJsonArray();
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for (JsonElement num : point) {
			if (!num.isJsonPrimitive())
				return null;
			if (!first)
				s.append(',');
			else
				first = false;
			double d = num.getAsDouble();
			s.append(d);
		}
		return s.toString();
	}

	private void startElems(String... names) throws Exception {
		for (String name : names)
			startElem(name);
	}

	private void startElem(String name) throws Exception {
		kml.writeCharacters("\n");
		for (int i = 0; i < indent; i++)
			kml.writeCharacters("  ");
		kml.writeStartElement(name);
		indent++;
	}

	private void endElems(int count) throws Exception {
		for (int i = 0; i < count; i++)
			endElem();
	}

	private void endElem() throws Exception {
		kml.writeCharacters("\n");
		indent--;
		for (int i = 0; i < indent; i++)
			kml.writeCharacters("  ");
		kml.writeEndElement();
	}

}
