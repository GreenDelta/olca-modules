package org.openlca.io.ilcd.output;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.ZipStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportConfig {

	private final static Logger log = LoggerFactory
			.getLogger(ExportConfig.class);
	public final IDatabase db;
	public final DataStore store;
	public String lang = "en";

	public ExportConfig(IDatabase database, File zip) {
		DataStore store = null;
		try {
			store = new ZipStore(zip);
		} catch (Exception e) {
			log.error("ILCD export failed", e);
		}
		this.db = database;
		this.store = store;
	}

	public ExportConfig(IDatabase database, DataStore store) {
		this.db = database;
		this.store = store;
	}

}
