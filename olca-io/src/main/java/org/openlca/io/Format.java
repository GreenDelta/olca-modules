package org.openlca.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

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


		return Optional.empty();
	}

	private static boolean hasExtension(String name, String ext) {
		if (name == null || ext == null)
			return false;
		return name.toLowerCase().endsWith(ext.toLowerCase());
	}

	private static Format fromXML(InputStream stream) {

		// read the root element
		QName qname =null;
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

}
