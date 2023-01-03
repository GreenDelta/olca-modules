package org.openlca.io;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.openlca.util.ZipFiles;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Get the list of units from a set of EcoSpold 01 files.
 */
public class EcoSpoldUnitFetch {

	private final TreeSet<String> units = new TreeSet<>();
	private final Handler handler = new Handler();
	private final SAXParser parser;

	public EcoSpoldUnitFetch() {
		try {
			var factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newSAXParser();
		} catch (Exception e) {
			throw new RuntimeException("Could not create parser", e);
		}
	}

	/**
	 * Get the units from the given files. Allowed input are zip and xml files.
	 */
	public String[] getUnits(File[] files) throws Exception {
		units.clear();
		for (File file : files) {
			if (StringUtils.endsWithIgnoreCase(file.getName(), ".xml"))
				parser.parse(file, handler);
			else if (StringUtils.endsWithIgnoreCase(file.getName(), ".zip")) {
				try (var zip = ZipFiles.open(file)) {
					parseZip(zip);
				}
			}
		}
		return units.toArray(new String[0]);
	}

	private void parseZip(ZipFile zipFile) throws Exception {
		final Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory())
				continue;
			if (StringUtils.endsWithIgnoreCase(entry.getName(), ".xml")) {
				try (InputStream in = zipFile.getInputStream(entry)) {
					parser.parse(in, handler);
				}
			}
		}
	}

	private class Handler extends DefaultHandler {

		private final String IMPACT_NS = "http://www.EcoInvent.org/EcoSpold01Impact";
		private boolean impactMethod = false;

		@Override
		public void startElement(
				String uri, String localName, String qName, Attributes attributes) {
			if ("ecoSpold".equals(qName) || "ecoSpold".equals(uri)) {
				impactMethod = IMPACT_NS.equals(uri);
			}
			if (impactMethod
					&& ("referenceFunction".equals(qName)
					|| "referenceFunction".equals(localName)))
				return; // do not fetch units from impact assessment categories
			String val = attributes.getValue("unit");
			if (val != null) {
				units.add(val);
			}
		}
	}

}
