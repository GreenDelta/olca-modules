package org.openlca.io.ecospold1.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openlca.commons.Res;
import org.openlca.util.ZipFiles;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/// Collects the units from a set of EcoSpold 01 files.
public class ES1UnitFetch {

	/// The result of a unit fetch: the sorted list of unit names and the files
	/// or zip entries that could not be parsed, each as `<path>: <reason>`.
	/// Zip entries are named `<zip file path>/<entry name>`.
	public record FetchResult(List<String> units, List<String> parserErrors) {
		public boolean hasParserErrors() {
			return !parserErrors.isEmpty();
		}
	}

	private final TreeSet<String> units = new TreeSet<>();
	private final List<String> parserErrors = new ArrayList<>();
	private final Handler handler = new Handler();
	private final SAXParser parser;

	private ES1UnitFetch(SAXParser parser) {
		this.parser = parser;
	}

	/// Collects the units from the given files. Allowed input are XML files and
	/// zip files with XML files; other files are ignored. Files and zip entries
	/// that cannot be parsed are skipped and are reported as parser errors in the
	/// result. An error is returned when a file cannot be read at all.
	public static Res<FetchResult> collectFrom(File[] files) {
		if (files == null)
			return Res.error("no files provided");
		try {

			var factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			var fetch = new ES1UnitFetch(factory.newSAXParser());
			for (var file : files) {
				if (file == null)
					continue;
				try {
					if (hasExt(file.getName(), ".xml")) {
						try (var in = Files.newInputStream(file.toPath())) {
							fetch.parse(file.getPath(), in);
						}
					} else if (hasExt(file.getName(), ".zip")) {
						fetch.parseZip(file);
					}
				} catch (IOException e) {
					return Res.error("Failed to read file "
						+ file.getPath() + ": " + e.getMessage(), e);
				}
			}

			var result = new FetchResult(
				new ArrayList<>(fetch.units), fetch.parserErrors);
			return Res.ok(result);
		} catch (Exception e) {
			return Res.error(
				"Failed to collect units from files: " + e.getMessage(), e);
		}
	}

	private void parseZip(File file) throws IOException {
		try (var zip = ZipFiles.open(file)) {
			var entries = zip.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				if (entry.isDirectory()
					|| !hasExt(entry.getName(), ".xml"))
					continue;
				try (var in = zip.getInputStream(entry)) {
					parse(file.getPath() + "/" + entry.getName(), in);
				}
			}
		}
	}

	private void parse(String path, InputStream in) throws IOException {
		try {
			parser.parse(in, handler);
		} catch (SAXException e) {
			parserErrors.add(path + ": " + e.getMessage());
			// make sure that the parser is in a clean state for the next file
			parser.reset();
		}
	}

	private static boolean hasExt(String s, String suffix) {
		if (s == null || suffix == null)
			return false;
		int offset = s.length() - suffix.length();
		return offset >= 0
			&& s.regionMatches(true, offset, suffix, 0, suffix.length());
	}

	private class Handler extends DefaultHandler {

		private boolean isImpactMethod = false;

		@Override
		public void startDocument() {
			// do not take the state of a previous document into a new one
			isImpactMethod = false;
		}

		@Override
		public void startElement(
			String uri, String localName, String qName, Attributes attributes
		) {
			if ("ecoSpold".equals(qName) || "ecoSpold".equals(localName)) {
				isImpactMethod = "http://www.EcoInvent.org/EcoSpold01Impact"
					.equals(uri);
			}
			if (isImpactMethod
				&& ("referenceFunction".equals(qName)
				|| "referenceFunction".equals(localName)))
				return; // do not fetch units from impact assessment categories
			var val = attributes.getValue("unit");
			if (val != null) {
				units.add(val);
			}
		}
	}

}
