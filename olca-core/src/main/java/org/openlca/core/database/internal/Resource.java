package org.openlca.core.database.internal;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Resource {

	CURRENT_SCHEMA_DERBY("current_schema_derby.sql");

	private final String file;

	Resource(String file) {
		this.file = file;
	}

	public InputStream getStream() {
		Logger log = LoggerFactory.getLogger(getClass());
		log.trace("load resource {} as stream", file);
		return getClass().getResourceAsStream(file);
	}

}
