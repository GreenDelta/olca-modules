package org.openlca.core.matrix.io.index;

import java.io.File;

/**
 * An enumeration of the standard formats in which a matrix index can be saved.
 */
public enum IxFormat {

	CSV(".csv"),

	PROTO(".bin");

	private final String extension;

	IxFormat(String extension) {
		this.extension = extension;
	}

	String name(String base) {
		return base + extension;
	}

	File file(File folder, String base) {
		return new File(folder, name(base));
	}

}
