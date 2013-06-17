package org.openlca.core.database.internal;

import java.io.InputStream;

public enum Resource {

	CURRENT_SCHEMA("current_schema_v1.4.sql"),

	REF_DATA_ALL("ref_data_all.sql"),

	REF_DATA_UNITS("ref_data_units.sql");

	private final String file;

	private Resource(String file) {
		this.file = file;
	}

	public InputStream getStream() {
		return getClass().getResourceAsStream(file);
	}

}
