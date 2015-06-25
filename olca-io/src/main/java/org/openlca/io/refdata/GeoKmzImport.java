package org.openlca.io.refdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;

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
import org.openlca.io.KeyGen;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		boolean nextIsShortName = false;
		String shortName = null;
		byte[] kmz = null;
		while (reader.hasNext()) {
			reader.next();
			if (isEnd(reader, "geography"))
				break;
			else if (isStart(reader, "shortname"))
				nextIsShortName = true;
			else if (reader.isCharacters() && nextIsShortName) {
				shortName = reader.getText();
				nextIsShortName = false;
			} else if (isStart(reader, "kml")) {
				kmz = getKmz(reader);
			}
		}
		checkUpdate(shortName, kmz);
		return kmz != null;
	}

	private void checkUpdate(String shortName, byte[] kmz) {
		if (shortName == null || kmz == null)
			return;
		log.trace("try insert KML for location {}", shortName);
		try {
			String refId = KeyGen.get(shortName);
			Location location = dao.getForRefId(refId);
			if (location == null) {
				log.info(
						"could not insert KML for location {} ({}); not found",
						shortName, refId);
				return;
			}
			location.setKmz(kmz);
			dao.update(location);
			log.trace("KML in location {} updated", shortName);
		} catch (Exception e) {
			log.error("failed to insert KML for location " + shortName, e);
		}
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
