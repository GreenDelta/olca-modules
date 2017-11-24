package org.openlca.io.refdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.UUID;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
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

import com.google.common.base.Strings;

public class GeoKmzImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final File file;
	private final LocationDao dao;

	private XMLStreamReader reader;
	private Transformer transformer;

	public GeoKmzImport(File file, IDatabase db) {
		this.file = file;
		dao = new LocationDao(db);
	}

	public boolean run() {
		boolean foundDataInFile = false;
		try (FileInputStream is = new FileInputStream(file)) {
			reader = XMLInputFactory.newFactory()
					.createXMLStreamReader(is);
			transformer = TransformerFactory.newInstance()
					.newTransformer();
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
		Loc loc = new Loc();
		readLonLat(loc);
		while (reader.hasNext()) {
			reader.next();
			if (isStart(reader, "name")) {
				String lang = reader.getAttributeValue(0);
				if (loc.name != null && !"en".equals(lang))
					continue;
				loc.name = readText();
			} else if (isStart(reader, "shortname")) {
				loc.shortName = readText();
			} else if (isStart(reader, "kml"))
				loc.kmz = getKmz(reader);
			if (isEnd(reader, "geography"))
				break;
		}
		insertOrUpdate(loc);
		return loc.kmz != null;
	}

	private void readLonLat(Loc loc) {
		String longitude = null;
		String latitude = null;
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String attributeName = reader.getAttributeLocalName(i);
			if ("longitude".equals(attributeName))
				longitude = reader.getAttributeValue(i);
			else if ("latitude".equals(attributeName))
				latitude = reader.getAttributeValue(i);
		}
		try {
			if (longitude != null)
				loc.longitude = Double.parseDouble(longitude);
			if (latitude != null)
				loc.latitude = Double.parseDouble(latitude);
		} catch (Exception e) {
			log.error("Invalid latitude or longitude "
					+ latitude + "," + longitude, e);
		}
	}

	private String readText() throws Exception {
		StringBuilder b = new StringBuilder();
		reader.next();
		while (reader.isCharacters()) {
			b.append(reader.getText());
			reader.next();
		}
		return b.toString();
	}

	private void insertOrUpdate(Loc loc) {
		if (loc == null || !loc.valid())
			return;
		log.trace("try insert KML for location {}", loc.name);
		try {
			String refId = KeyGen.get(loc.shortName);
			Location location = dao.getForRefId(refId);
			if (location == null) {
				insert(loc);
			} else if (loc.kmz != null) {
				update(location, loc.kmz);
			}
		} catch (Exception e) {
			log.error("failed to insert KML for location " + loc.shortName, e);
		}
	}

	private void insert(Loc loc) {
		Location location = new Location();
		location.setName(loc.name);
		if (Strings.isNullOrEmpty(loc.shortName))
			location.setRefId(UUID.randomUUID().toString());
		else
			location.setRefId(KeyGen.get(loc.shortName));
		location.setCode(loc.shortName);
		location.setLongitude(loc.longitude);
		location.setLatitude(loc.latitude);
		location.setKmz(loc.kmz);
		dao.insert(location);
		log.trace("New location added {}", loc.name);
	}

	private void update(Location location, byte[] kmz) {
		location.setKmz(kmz);
		location.setLastChange(Calendar.getInstance().getTimeInMillis());
		Version.incUpdate(location);
		dao.update(location);
		log.trace("KML in location {} updated", location.getName());
	}

	private byte[] getKmz(XMLStreamReader reader) {
		try {
			DOMResult dom = new DOMResult();
			transformer.transform(new StAXSource(reader), dom);
			Document doc = (Document) dom.getNode();
			NodeList list = doc.getElementsByTagName("*");
			String ns = "http://earth.google.com/kml/2.1";
			for (int i = 0; i < list.getLength(); i++) {
				Node n = list.item(i);
				doc.renameNode(n, ns, n.getLocalName());
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(doc), new StreamResult(bout));
			byte[] bytes = bout.toByteArray();
			return BinUtils.zip(bytes);
		} catch (Exception e) {
			log.error("failed to parse KML", e);
			return null;
		}
	}

	private boolean isStart(XMLStreamReader reader, String tagName) {
		return reader.isStartElement() && reader.getLocalName().equals(tagName);
	}

	private boolean isEnd(XMLStreamReader reader, String tagName) {
		return reader.isEndElement() && reader.getLocalName().equals(tagName);
	}

	/** Internal class for parsed data. */
	private class Loc {
		String name;
		String shortName;
		double longitude;
		double latitude;
		byte[] kmz;

		boolean valid() {
			return name != null && shortName != null;
		}
	}
}
