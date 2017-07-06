package org.openlca.io.ilcd.input;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportConfig {

	private final static Logger log = LoggerFactory
			.getLogger(ImportConfig.class);
	public final DataStore store;
	public final IDatabase db;
	public final FlowMap flowMap;
	public boolean importFlows;
	public String[] langs = { "en" };

	public ImportConfig(File zip, IDatabase database) {
		DataStore store = null;
		try {
			store = new ZipStore(zip);
		} catch (Exception e) {
			log.error("ILCD import failed", e);
		}
		this.store = store;
		this.db = database;
		this.flowMap = new FlowMap(Maps.ILCD_FLOW_IMPORT, database);
	}

	public ImportConfig(DataStore store, IDatabase database) {
		this.store = store;
		this.db = database;
		this.flowMap = new FlowMap(Maps.ILCD_FLOW_IMPORT, database);
	}

}
