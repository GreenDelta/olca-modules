package org.openlca.io.refdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.util.BinUtils;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class GeoKmzImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final File file;
	private final LocationDao dao;

	private XMLStreamReader reader;
	private Transformer transformer;
	private SAXBuilder builder;

	public GeoKmzImport(File file, IDatabase database) {
		this.file = file;
		dao = new LocationDao(database);
	}

	public boolean run() {
		boolean foundDataInFile = false;
		try {
			setUp();
			while (reader.hasNext()) {
				reader.next();
				if (isStart(reader, "geography"))
					if (handleGeography())
						foundDataInFile = true;
			}
			reader.close();
			return foundDataInFile;
		} catch (Exception e) {
			log.error("failed to import KML data for geographies", e);
			return false;
		}
	}

	private void setUp() throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newFactory();
		reader = inputFactory.createXMLStreamReader(new FileInputStream(file));
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		transformer = transformerFactory.newTransformer();
		builder = new SAXBuilder();
	}

	private boolean handleGeography() throws Exception {
		String longitude = null;
		String latitude = null;
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String attributeName = reader.getAttributeLocalName(i);
			if ("longitude".equals(attributeName))
				longitude = reader.getAttributeValue(i);
			else if ("latitude".equals(attributeName))
				latitude = reader.getAttributeValue(i);
		}
		String name = null;
		String shortName = null;
		byte[] kmz = null;
		while (reader.hasNext()) {
			reader.next();
			if (isStart(reader, "name")) {
				String lang = reader.getAttributeValue(0);
				if (name != null && !"en".equals(lang))
					continue;
				reader.next();
				if (reader.isCharacters())
					name = reader.getText();
			} else if (isStart(reader, "shortname")) {
				reader.next();
				if (reader.isCharacters())
					shortName = reader.getText();
			} else if (isStart(reader, "kml"))
				kmz = getKmz(reader);
			if (isEnd(reader, "geography"))
				break;
		}
		double lo = parseDouble(longitude);
		double la = parseDouble(latitude);
		insertOrUpdate(name, shortName, lo, la, kmz);
		return kmz != null;
	}

	private double parseDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NullPointerException | NumberFormatException e) {
			return 0;
		}
	}

	private void insertOrUpdate(String name, String shortName,
			double longitude, double latitude, byte[] kmz) {
		if (name == null || shortName == null || kmz == null)
			return;
		log.trace("try insert KML for location {}", name);
		try {
			String refId = KeyGen.get(shortName);
			Location location = dao.getForRefId(refId);
			if (location == null)
				insert(name, shortName, longitude, latitude, kmz);
			else
				update(location, kmz);
		} catch (Exception e) {
			log.error("failed to insert KML for location " + shortName, e);
		}
	}

	private void insert(String name, String shortName, double longitude,
			double latitude, byte[] kmz) {
		Location location = new Location();
		location.setName(name);
		if (Strings.isNullOrEmpty(shortName))
			location.setRefId(UUID.randomUUID().toString());
		else
			location.setRefId(KeyGen.get(shortName));
		location.setCode(shortName);
		location.setLongitude(longitude);
		location.setLatitude(latitude);
		location.setKmz(kmz);
		dao.insert(location);
		log.trace("KML added as new location {}", name);
	}

	private void update(Location location, byte[] kmz) {
		location.setKmz(kmz);
		dao.update(location);
		log.trace("KML in location {} updated", location.getName());
	}

	private byte[] getKmz(XMLStreamReader reader) {
		try {
			StringWriter writer = new StringWriter();
			transformer.transform(new StAXSource(reader), new StreamResult(
					writer));
			StringReader source = new StringReader(writer.toString());
			Document doc = builder.build(source);
			Namespace ns = Namespace
					.getNamespace("http://earth.google.com/kml/2.1");
			switchNamespace(doc.getRootElement(), ns);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			new XMLOutputter().output(doc, bout);
			return BinUtils.zip(bout.toByteArray());
		} catch (Exception e) {
			log.error("failed to parse KML", e);
			return null;
		}
	}

	private void switchNamespace(Element element, Namespace namespace) {
		element.setNamespace(namespace);
		for (Element child : element.getChildren()) {
			switchNamespace(child, namespace);
		}
	}

	private boolean isStart(XMLStreamReader reader, String tagName) {
		return reader.isStartElement() && reader.getLocalName().equals(tagName);
	}

	private boolean isEnd(XMLStreamReader reader, String tagName) {
		return reader.isEndElement() && reader.getLocalName().equals(tagName);
	}
}
