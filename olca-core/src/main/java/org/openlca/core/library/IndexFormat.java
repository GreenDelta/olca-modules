package org.openlca.core.library;

import java.io.File;

public enum IndexFormat {

	CSV(".csv"),

	PROTO(".bin");

	private final String extension;

	IndexFormat(String extension) {
		this.extension = extension;
	}

	String name(String base) {
		return base + extension;
	}

	File file(Library lib, String base) {
		return new File(lib.folder(), name(base));
	}

}
