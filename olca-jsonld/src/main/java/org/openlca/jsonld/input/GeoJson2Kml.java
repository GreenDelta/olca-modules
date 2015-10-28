package org.openlca.jsonld.input;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
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
		startElem("Folder");
		startElem("Placemark");
		writeGeometry(geoJson);
		endElem();
		endElem();
		endElem();
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
		}
	}

	private void writePoint(JsonObject geoJson) throws Exception {
		String coordinate = getCoordinate(geoJson.get("coordinates"));
		if (coordinate == null)
			return;
		startElem("Point");
		startElem("coordinates");
		kml.writeCharacters("\n");
		for (int i = 0; i < indent; i++)
			kml.writeCharacters("  ");
		kml.writeCharacters(coordinate);
		endElem();
		endElem();
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

	private void startElem(String name) throws Exception {
		kml.writeCharacters("\n");
		for (int i = 0; i < indent; i++)
			kml.writeCharacters("  ");
		kml.writeStartElement(name);
		indent++;
	}

	private void endElem() throws Exception {
		kml.writeCharacters("\n");
		indent--;
		for (int i = 0; i < indent; i++)
			kml.writeCharacters("  ");
		kml.writeEndElement();
	}

}
