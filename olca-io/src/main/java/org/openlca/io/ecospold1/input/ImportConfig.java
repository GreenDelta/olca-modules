package org.openlca.io.ecospold1.input;

import org.openlca.core.database.IDatabase;
import org.openlca.io.UnitMapping;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.Maps;

public class ImportConfig {

	public final IDatabase db;

	private FlowMap flowMap;
	private UnitMapping unitMapping;

	public ImportConfig(IDatabase db) {
		this.db = db;
	}

	public FlowMap getFlowMap() {
		if (flowMap == null) {
			flowMap = new FlowMap(Maps.ES1_FLOW_IMPORT, db);
		}
		return flowMap;
	}

	public void setFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
	}

	public void setUnitMapping(UnitMapping unitMapping) {
		this.unitMapping = unitMapping;
	}

	public UnitMapping getUnitMapping() {
		if (unitMapping == null) {
			unitMapping = UnitMapping.createDefault(db);
		}
		return unitMapping;
	}

}
