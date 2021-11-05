package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.FlowMap;

class RefDataSync {

	private final IDatabase db;
	private final SpRefDataIndex index;
	private final FlowMap flowMap;

	public RefDataSync(
			SpRefDataIndex index,
			IDatabase db,
			FlowMap flowMap) {
		this.db = db;
		this.index = index;
		this.flowMap = flowMap;
	}

	public RefData run() {
		new FlowSync(index, refData.getUnitMapping(), db)
				.run(refData, flowMap);
		return refData;
	}

}
