package org.openlca.jsonld.output;

import java.io.StringReader;
import java.util.Scanner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * A minimal converter of KML to GeoJSON (as used in openLCA).
 * 
 * @see https://developers.google.com/kml/documentation/kmlreference
 * @see http://geojson.org/geojson-spec.html
 */
class Kml2GeoJson {

	private Kml2GeoJson() {
	}

	static JsonObject convert(String kml) {
		if (kml == null)
			return null;
		try {
			return new Kml2GeoJson().parse(kml);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Kml2GeoJson.class);
			log.error("failed to parse kml " + Strings.cut(kml, 75), e);
			return null;
		}
	}

	private JsonObject parse(String kml) throws Exception {
		XMLInputFactory xif = XMLInputFactory.newFactory();
		try (StringReader string = new StringReader(kml)) {
			XMLEventReader events = xif.createXMLEventReader(string);
			while (events.hasNext()) {
				XMLEvent event = events.nextEvent();
				if (isStartElement(event, "Placemark")) {
					return readGeometry(events);
				}
			}
		}
		return null;
	}

	private JsonObject readGeometry(XMLEventReader events) throws Exception {
		while (events.hasNext()) {
			XMLEvent event = events.nextEvent();
			if (isStartElement(event, "Point"))
				return readPoint(events);
			if (isStartElement(event, "LineString"))
				return readLineString(events);
			if (isStartElement(event, "LinearRing"))
				return readLineString(events);
			if (isStartElement(event, "Polygon"))
				return readPolygon(events);
			if (isStartElement(event, "MultiGeometry"))
				return readMultiGeometry(events);
		}
		return null;
	}

	private JsonObject readPoint(XMLEventReader events) throws Exception {
		JsonObject point = new JsonObject();
		point.addProperty("type", "Point");
		StringBuilder coordinates = null;
		while (events.hasNext()) {
			XMLEvent event = events.nextEvent();
			if (isStartElement(event, "coordinates"))
				coordinates = new StringBuilder();
			if (isEndElement(event, "coordinates"))
				break;
			if (event.isCharacters() && coordinates != null) {
				Characters chars = event.asCharacters();
				coordinates.append(chars.getData());
			}
		}
		if (coordinates != null) {
			point.add("coordinates", readCoordinate(coordinates.toString()));
		}
		return point;
	}

	private JsonObject readLineString(XMLEventReader events) throws Exception {
		JsonObject line = new JsonObject();
		line.addProperty("type", "LineString");
		StringBuilder coordinates = null;
		while (events.hasNext()) {
			XMLEvent event = events.nextEvent();
			if (isStartElement(event, "coordinates"))
				coordinates = new StringBuilder();
			if (isEndElement(event, "coordinates"))
				break;
			if (event.isCharacters() && coordinates != null) {
				Characters chars = event.asCharacters();
				coordinates.append(chars.getData());
			}
		}
		if (coordinates != null)
			line.add("coordinates", readCoordinates(coordinates.toString()));
		return line;
	}

	private JsonObject readPolygon(XMLEventReader events) throws Exception {
		JsonObject polygon = new JsonObject();
		polygon.addProperty("type", "Polygon");
		JsonObject outerBoundary = null;
		JsonObject innerBoundary = null;
		int boundaryType = 0; // 0 = undefined; 1 = outer; 2 = inner
		while (events.hasNext()) {
			XMLEvent event = events.nextEvent();
			if (isStartElement(event, "outerBoundaryIs")) {
				boundaryType = 1;
				continue;
			}
			if (isStartElement(event, "innerBoundaryIs")) {
				boundaryType = 2;
				continue;
			}
			if (isStartElement(event, "LinearRing")) {
				if (boundaryType == 1)
					outerBoundary = readLineString(events);
				else if (boundaryType == 2)
					innerBoundary = readLineString(events);
				boundaryType = 0;
			}
		}
		JsonArray coordinates = new JsonArray();
		polygon.add("coordinates", coordinates);
		if (outerBoundary != null) {
			coordinates.add(outerBoundary.get("coordinates"));
			if (innerBoundary != null) {
				coordinates.add(innerBoundary.get("coordinates"));
			}
		}
		return polygon;
	}

	private JsonObject readMultiGeometry(XMLEventReader events) throws Exception {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", "GeometryCollection");
		JsonArray geometries = new JsonArray();
		obj.add("geometries", geometries);
		JsonObject geo;
		while ((geo = readGeometry(events)) != null) {
			geometries.add(geo);
		}
		return obj;
	}

	private boolean isStartElement(XMLEvent event, String name) {
		if (!event.isStartElement())
			return false;
		StartElement elem = event.asStartElement();
		String n = elem.getName().getLocalPart();
		return name.equals(n);
	}

	private boolean isEndElement(XMLEvent event, String name) {
		if (!event.isEndElement())
			return false;
		EndElement elem = event.asEndElement();
		String n = elem.getName().getLocalPart();
		return name.equals(n);
	}

	private JsonArray readCoordinates(String s) {
		JsonArray array = new JsonArray();
		try (Scanner scanner = new Scanner(s)) {
			while (scanner.hasNext()) {
				JsonArray coordinate = readCoordinate(scanner.next());
				array.add(coordinate);
			}
		}
		return array;
	}

	private JsonArray readCoordinate(String s) {
		String[] parts = s.split(",");
		JsonArray array = new JsonArray();
		for (int i = 0; i < parts.length; i++) {
			double num = Double.parseDouble(parts[i]);
			array.add(new JsonPrimitive(num));
		}
		return array;
	}

}
