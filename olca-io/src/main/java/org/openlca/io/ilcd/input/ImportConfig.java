package org.openlca.io.ilcd.input;

import java.util.Arrays;
import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Flow;
import org.openlca.ilcd.io.DataStore;
import org.openlca.io.maps.FlowMap;
import org.openlca.util.Strings;

public class ImportConfig {

	private final DataStore store;
	private final IDatabase db;
	private final ImportLog log;

	private boolean allFlows;
	private String[] langOrder = { "en" };
	private HashMap<String, Flow> flowCache = new HashMap<>();
	private FlowMap flowMap;

	public ImportConfig(DataStore store, IDatabase database) {
		this.store = store;
		this.db = database;
		this.log = new ImportLog();
	}

	public ImportConfig withFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
		return this;
	}

	public ImportConfig withAllFlows(boolean b) {
		allFlows = b;
		return this;
	}

	/**
	 * Define the order in which a multi-language string should be evaluated. It
	 * first checks if there is a string for the first language of this list, then
	 * the second, etc.
	 */
	public ImportConfig withLanguageOrder(String ... codes) {
		if (codes != null && codes.length > 0) {
			var filtered = Arrays.stream(codes)
				.filter(Strings::notEmpty)
				.map(s -> s.trim().toLowerCase())
				.toArray(String[]::new);
			if (filtered.length > 0) {
				langOrder = filtered;
			}
		}
		return this;
	}

	public ImportLog log() {
		return log;
	}

	public DataStore store() {
		return store;
	}

	IDatabase db() {
		return db;
	}

	public boolean withAllFlows() {
		return allFlows;
	}

	String[] langOrder() {
		return langOrder;
	}

	HashMap<String, Flow> flowCache() {
		return flowCache;
	}

	FlowMap flowMap() {
		if (flowMap == null) {
			flowMap = FlowMap.empty();
		}
		return flowMap;
	}
}
