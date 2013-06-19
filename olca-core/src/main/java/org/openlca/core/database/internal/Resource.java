package org.openlca.core.database.internal;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Resource {

	CURRENT_SCHEMA_DERBY("current_schema_derby.sql"),

	CURRENT_SCHEMA_MYSQL("current_schema_mysql.sql"),

	REF_DATA_ALL("ref_data_all.sql"),

	REF_DATA_UNITS("ref_data_units.sql");

	private final String file;

	private Resource(String file) {
		this.file = file;
	}

	public InputStream getStream() {
		Logger log = LoggerFactory.getLogger(getClass());
		log.trace("load resource {} as stream", file);
		return getClass().getResourceAsStream(file);
	}

}
