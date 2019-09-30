package org.openlca.io.ecospold2.input;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.core.model.Version;
import org.openlca.util.BinUtils;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Strings;

/**
 * Imports locations and KML data from an EcoSpold 2 geographies master data
 * file (Geographies.xml, see https://geography.ecoinvent.org/). Locations are
 * identified by their location code. If a location already exists and has no
 * KML data assigned, it is updated with the KML data from the master data file.
 * New locations are created with the information from the master data.
 */
public class KMLImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final File file;
	private final LocationDao dao;

	private XMLStreamReader reader;
	private final Transformer transformer;
	private final DocumentBuilder docBuilder;

	public KMLImport(File file, IDatabase db) {
		this.file = file;
		dao = new LocationDao(db);
		try {
			transformer = TransformerFactory.newInstance()
					.newTransformer();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			docBuilder = factory.newDocumentBuilder();
		} catch (Exception e) {
			throw new RuntimeException("faile to initialie XML machinery", e);
		}
	}

	public boolean run() {
		boolean foundDataInFile = false;
		try (FileInputStream is = new FileInputStream(file)) {
			reader = XMLInputFactory.newFactory()
					.createXMLStreamReader(is);
			while (reader.hasNext()) {
				reader.next();
				if (isStart(reader, "geography")) {
					foundDataInFile = handleGeography();
				}
			}
			reader.close();
			return foundDataInFile;
		} catch (Exception e) {
			log.error("failed to import KML data for geographies", e);
			return false;
		}
	}

	private boolean handleGeography() throws Exception {
		Location loc = new Location();
		readLonLat(loc);
		while (reader.hasNext()) {
			reader.next();
			if (isStart(reader, "name")) {
				String lang = reader.getAttributeValue(0);
				if (loc.name != null && !"en".equals(lang))
					continue;
				loc.name = readText();
			} else if (isStart(reader, "shortname")) {
				loc.code = readText();
			} else if (isStart(reader, "kml")) {
				loc.kmz = getKmz(reader);
			}
			if (isEnd(reader, "geography"))
				break;
		}
		insertOrUpdate(loc);
		return loc.kmz != null;
	}

	private void readLonLat(Location loc) {
		String lon = null;
		String lat = null;
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String att = reader.getAttributeLocalName(i);
			if ("longitude".equals(att)) {
				lon = reader.getAttributeValue(i);
			} else if ("latitude".equals(att)) {
				lat = reader.getAttributeValue(i);
			}
		}
		try {
			if (lon != null)
				loc.longitude = Double.parseDouble(lon);
			if (lat != null)
				loc.latitude = Double.parseDouble(lat);
		} catch (Exception e) {
			log.error("Invalid latitude or longitude "
					+ lat + "," + lon, e);
		}
	}

	private String readText() throws Exception {
		StringBuilder b = new StringBuilder();
		reader.next();
		while (reader.isCharacters()) {
			b.append(reader.getText());
			reader.next();
		}
		return b.toString().trim();
	}

	private void insertOrUpdate(Location newLoc) {
		if (newLoc == null
				|| Strings.isNullOrEmpty(newLoc.name)
				|| Strings.isNullOrEmpty(newLoc.code))
			return;
		log.trace("try insert KML for location {}", newLoc.name);
		try {
			String refId = KeyGen.get(newLoc.code);
			Location existing = dao.getForRefId(refId);
			long time = Calendar.getInstance().getTimeInMillis();
			if (existing == null) {
				// insert as new location
				newLoc.refId = refId;
				newLoc.lastChange = time;
				dao.insert(newLoc);
				log.trace("New location added {}", newLoc.name);
			} else if (existing.kmz == null && newLoc.kmz != null) {
				existing.lastChange = time;
				Version.incUpdate(existing);
				existing.kmz = newLoc.kmz;
				dao.update(existing);
				log.trace("KML in location {} updated", existing.name);
			}
		} catch (Exception e) {
			log.error("failed to insert or update KML for location "
					+ newLoc.code, e);
		}
	}

	private byte[] getKmz(XMLStreamReader reader) {
		try {
			// we need to apply some steps in order to get the
			// KML XML:
			// * get the XML from the event as byte array
			// * convert it to a DOM
			// * set the KML namespace
			// * finally, convert it to zipped bytes ...
			// in earlier versions we directly created the
			// DOM via DOMResult, but this does not work with
			// the OpenJDK 8? https://bugs.openjdk.java.net/browse/JDK-8016914

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			StreamResult stream = new StreamResult(bout);
			transformer.transform(new StAXSource(reader), stream);
			InputSource is = new InputSource(
					new ByteArrayInputStream(bout.toByteArray()));
			Document doc = docBuilder.parse(is);
			NodeList list = doc.getElementsByTagName("*");
			String ns = "http://www.opengis.net/kml/2.2";
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				doc.renameNode(n, ns, n.getLocalName());
			}
			bout = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(doc), new StreamResult(bout));
			byte[] bytes = bout.toByteArray();
			return BinUtils.zip(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("failed to parse KML", e);
			return null;
		}
	}

	private boolean isStart(XMLStreamReader reader, String tagName) {
		return reader.isStartElement()
				&& reader.getLocalName().equals(tagName);
	}

	private boolean isEnd(XMLStreamReader reader, String tagName) {
		return reader.isEndElement()
				&& reader.getLocalName().equals(tagName);
	}

}
