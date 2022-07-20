package org.openlca.core.matrix.io.index;

import org.openlca.core.library.Library;

import java.io.File;

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

	File file(Library lib, String base) {
		return new File(lib.folder(), name(base));
	}

}
