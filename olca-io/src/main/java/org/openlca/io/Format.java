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

import org.apache.commons.io.input.XmlStreamReader;

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
	 * A zip file with ILCD files.
	 */
	ILCD_ZIP,

	/**
	 * A zip file with JSON(-LD) files in the openLCA Schema format.
 	 */
	JSON_LD_ZIP,

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

		// ZOLCA
		if (hasExtension(fileName, ".zolca"))
			return Optional.of(ZOLCA);

		// *.spold => EcoSpold 2
		if (hasExtension(fileName, ".spold"))
			return Optional.of(ES2_XML);

		if (hasExtension(fileName, ".xml")) {
			try (var stream = new FileInputStream(file);
				var buffer = new BufferedInputStream(stream)){
				var root = peekXmlRoot(buffer);
				if (root == null)
					return Optional.empty();

				if (Objects.equals(root.getLocalPart(), "ecoSpold"))
					return Optional.of(ES1_XML);

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

	private static QName peekXmlRoot(InputStream stream) {
		try {
			var reader = XMLInputFactory.newInstance()
					.createXMLStreamReader(stream);
			while (reader.hasNext()) {
				int next = reader.next();
				if (next != XMLStreamReader.START_ELEMENT)
					continue;
				return reader.getName();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

}
