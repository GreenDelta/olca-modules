package org.openlca.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.openlca.util.Strings;

/**
 * A set of import formats that openLCA understands and that can be determined
 * from a file.
 */
public enum Format {

	/**
	 * An EcoSpold1 XML data set.
	 */
	ES1_XML,

	/**
	 * A zip file with EcoSpold 1 XML data sets.
	 */
	ES1_ZIP,

	/**
	 * An EcoSpold 2 XML data set. These can have an *.xml or *.spold extension.
	 */
	ES2_XML,

	/**
	 * A zip file with EcoSpold 2 XML data sets.
	 */
	ES2_ZIP,

	/**
	 * An Excel file with openLCA data sets.
	 */
	EXCEL,

	/**
	 * A GeoJSON file.
	 */
	GEO_JSON,

	/**
	 * A zip file with ILCD files.
	 */
	ILCD_ZIP,

	/**
	 * A zip file with JSON(-LD) files in the openLCA Schema format.
	 */
	JSON_LD_ZIP,

	/**
	 * A KML file.
	 */
	KML,

	/**
	 * A zip file that contains an openLCA libraries with its dependencies.
	 */
	LIBRARY_PACKAGE,

	/**
	 * An openLCA flow mapping file in CSV format:
	 * - columns separated by semicolons
	 * - at minimum 3 columns
	 * - the third column contains numbers
	 */
	MAPPING_CSV,

	/**
	 * A SimaPro CSV file.
	 */
	SIMAPRO_CSV,

	/**
	 * A *.zolca file is a zip file that contains a Derby database.
	 */
	ZOLCA;


	/**
	 * Tries to detect the format from the given file. Returns `Optional.empty`
	 * if the format cannot be detected or if an error occurred.
	 */
	public static Optional<Format> detect(File file) {
		if (file == null)
			return Optional.empty();
		var fileName = file.getName();

		// *.zolca
		if (hasExtension(fileName, ".zolca"))
			return Optional.of(ZOLCA);

		// *.geojson
		if (hasExtension(fileName, ".geojson"))
			return Optional.of(GEO_JSON);

		// *.spold => EcoSpold 2
		if (hasExtension(fileName, ".spold"))
			return Optional.of(ES2_XML);

		// *.kml
		if (hasExtension(fileName, ".kml"))
			return Optional.of(KML);

		// *.xlsx
		if (hasExtension(fileName, ".xlsx"))
			return Optional.of(EXCEL);

		// *.xml => check if the format is known
		if (hasExtension(fileName, ".xml")) {
			try (var stream = new FileInputStream(file);
				 var buffer = new BufferedInputStream(stream)) {
				var format = fromXML(buffer);
				return Optional.ofNullable(format);
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		// *.csv => check if it is SimaPro CSV or a mapping file
		if (hasExtension(fileName, ".csv")) {
			try (var stream = new FileInputStream(file);
				 var reader = new InputStreamReader(stream);
				 var buffer = new BufferedReader(reader)) {
				var first = buffer.readLine();

				// check if it is SimaPro CSV
				if (first.startsWith("{SimaPro "))
					return Optional.of(SIMAPRO_CSV);

				// check if it is a mapping file
				var columns = first.split(";");
				if (columns.length < 3)
					return Optional.empty();
				try {
					Double.parseDouble(columns[2]);
					return Optional.of(MAPPING_CSV);
				} catch (Exception ignored) {
				}
				return Optional.empty();
			} catch (Exception e) {
				return Optional.empty();
			}
		}

		// check *.zip files
		if (!hasExtension(fileName, ".zip"))
			return Optional.empty();
		var formatRef = new AtomicReference<Format>();
		scanZip(file, (zip, entry) -> {
			var entryName = entry.getName();

			// library package
			if (Strings.nullOrEqual(entryName, "library.json")) {
				formatRef.set(LIBRARY_PACKAGE);
				return true;
			}

			// ES2
			if (hasExtension(entryName, ".spold")) {
				formatRef.set(ES2_ZIP);
				return true;
			}

			// JSON-LD
			if (hasExtension(entryName, ".json")) {
				if (hasPathOneOf(entryName,
						"actors",
						"categories",
						"currencies",
						"dq_systems",
						"flow_properties",
						"flows",
						"lcia_categories",
						"lcia_methods",
						"locations",
						"nw_sets",
						"parameters",
						"processes",
						"product_systems",
						"projects",
						"social_indicators",
						"sources",
						"unit_groups")) {
					formatRef.set(JSON_LD_ZIP);
					return true;
				}
			}

			if (!hasExtension(entryName, ".xml"))
				return false;

			// XML files in the ILCD package Layout
			if (hasPathOneOf(entryName,
					"ILCD",
					"contacts",
					"flowproperties",
					"flows",
					"lciamethods",
					"lifecyclemodels",
					"processes",
					"sources",
					"unitgroups")) {
				formatRef.set(ILCD_ZIP);
				return true;
			}

			// parse XML files in zip
			try (var stream = zip.getInputStream(entry)) {
				var format = fromXML(stream);
				if (format == null)
					return false;
				switch (format) {
					case ES1_XML:
						formatRef.set(ES1_ZIP);
						return true;
					case ES2_XML:
						formatRef.set(ES2_ZIP);
						return true;
				}
			} catch (Exception ignored) {
			}
			return false;
		});

		return Optional.ofNullable(formatRef.get());
	}

	private static boolean hasExtension(String name, String ext) {
		if (name == null || ext == null)
			return false;
		return name.toLowerCase().endsWith(ext.toLowerCase());
	}

	private static Format fromXML(InputStream stream) {

		// read the root element
		QName qname = null;
		try {
			var reader = XMLInputFactory.newInstance()
					.createXMLStreamReader(stream);
			while (reader.hasNext()) {
				int next = reader.next();
				if (next != XMLStreamReader.START_ELEMENT)
					continue;
				qname = reader.getName();
				break;
			}
		} catch (Exception e) {
			return null;
		}

		if (qname == null)
			return null;

		if (Objects.equals("ecoSpold", qname.getLocalPart())) {

			// EcoSpold 1
			if (Objects.equals(qname.getNamespaceURI(),
					"http://www.EcoInvent.org/EcoSpold01")) {
				return ES1_XML;
			}
			if (Objects.equals(qname.getNamespaceURI(),
					"http://www.EcoInvent.org/EcoSpold01Impact")) {
				return ES1_XML;
			}

			// EcoSpold 2
			if (Objects.equals(qname.getNamespaceURI(),
					"http://www.EcoInvent.org/EcoSpold02")) {
				return ES2_XML;
			}
		}

		if (Objects.equals("kml", qname.getLocalPart()))
			return KML;

		return null;
	}

	/**
	 * Scans each entry in the given zip file until the given function returns
	 * true.
	 */
	private static void scanZip(File zipFile, BiPredicate<ZipFile, ZipEntry> fn) {
		try (var zip = new ZipFile(zipFile)) {
			var entries = zip.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				if (fn.test(zip, entry))
					break;
			}
		} catch (Exception ignored) {
		}
	}

	private static boolean hasPathOneOf(String path, String... parts) {
		if (path == null)
			return false;
		var pathParts = path.split("[/\\\\]");
		for (var pathPart : pathParts) {
			for (var part : parts) {
				if (part.equalsIgnoreCase(pathPart))
					return true;
			}
		}
		return false;
	}
}
