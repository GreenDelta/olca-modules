package org.openlca.io.ecospold2.input;

import java.util.Collections;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;

/**
 * Import configuration for EcoSpold 02 data sets.
 */
public class ImportConfig {

	/**
	 * If true, exchanges with a value of 0 will not be imported (in ecoinvent 3
	 * there are a lot of such exchanges).
	 */
	public boolean skipNullExchanges = false;

	/**
	 * If true, imports parameters from EcoSpold 02 data sets.
	 */
	public boolean withParameters = true;

	/**
	 * If true, parameter formulas are imported ((in ecoinvent 3 there are a lot
	 * of parameters that cannot be evaluated in openLCA).
	 */
	public boolean withParameterFormulas = true;

	/**
	 * If true, formulas that contain functions that are not available in
	 * openLCA are filtered.
	 */
	public boolean checkFormulas = false;

	public final IDatabase db;
	private final ImportLog log = new ImportLog();
	private Map<String, FlowMapEntry> flowMap;

	public ImportConfig(IDatabase db) {
		this.db = db;
	}

	public void setFlowMap(FlowMap flowMap) {
		if (flowMap != null) {
			this.flowMap = flowMap.index();
		}
	}

	Map<String, FlowMapEntry> getFlowMap() {
		return flowMap == null
			? Collections.emptyMap()
			: flowMap;
	}

	ImportLog log() {
		return log;
	}
}
