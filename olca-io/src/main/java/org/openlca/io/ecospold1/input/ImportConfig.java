package org.openlca.io.ecospold1.input;

import org.openlca.core.database.IDatabase;
import org.openlca.io.UnitMapping;
import org.openlca.io.maps.FlowMap;

public class ImportConfig {

	public final IDatabase db;

	private FlowMap flowMap;
	private UnitMapping unitMapping;

	public ImportConfig(IDatabase db) {
		this.db = db;
	}

	public FlowMap getFlowMap() {
		if (flowMap == null) {
			flowMap = FlowMap.empty();
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
