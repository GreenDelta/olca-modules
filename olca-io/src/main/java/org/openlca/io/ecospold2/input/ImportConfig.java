package org.openlca.io.ecospold2.input;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.Maps;

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
	 * If true, parameter formulas are imported ((in ecoinvent 3 there are a lot of
	 * parameters that cannot be evaluated in openLCA).
	 */
	public boolean withParameterFormulas = true;

	/**
	 * If true, formulas that contain functions that are not available in openLCA
	 * are filtered.
	 */
	public boolean checkFormulas = false;

	public final IDatabase db;

	private FlowMap flowMap;

	public ImportConfig(IDatabase db) {
		this.db = db;
	}

	public FlowMap getFlowMap() {
		if (flowMap == null) {
			flowMap = new FlowMap(Maps.ES2_FLOW_IMPORT, db);
		}
		return flowMap;
	}

	public void setFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
	}

}
