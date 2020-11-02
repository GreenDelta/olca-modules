package org.openlca.io;

import java.io.File;
import java.util.Optional;

/**
 * A set of import formats that openLCA understands and that can be determined
 * from a file.
 */
public enum Format {

	/**
	 * A EcoSpold1 data set.
	 */
	ES1_XML,

	/**
	 * A zip file with EcoSpold 1 data sets.
	 */
	ES1_ZIP,

	/**
	 * A zip file with ILCD files.
	 */
	ILCD_ZIP,

	/**
	 * A zip file with JSON(-LD) files in the openLCA Schema format.
 	 */
	JSON_LD_ZIP,

	/**
	 * A *.zolca file is a zip file that contains a Derby database.
	 */
	ZOLCA;


	public static Optional<Format> detect(File file) {
		if (file == null)
			return Optional.empty();
		if (hasExtension(file.getName(), ".zolca"))
			return Optional.of(ZOLCA);

		return Optional.empty();
	}

	private static boolean hasExtension(String name, String ext) {
		if (name == null || ext == null)
			return false;
		return name.toLowerCase().endsWith(ext.toLowerCase());
	}

}
