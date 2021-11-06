package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Source;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.CsvDataSet;

class RefData {

	private final IDatabase db;
	private final UnitSync unitSync;
	private final SourceSync sourceSync;
	private final FlowSync flowSync;

	RefData(IDatabase db, FlowMap flowMap) {
		this.db = db;
		this.unitSync = new UnitSync(db);
		this.sourceSync = new SourceSync(db);
		this.flowSync = new FlowSync(db, this, flowMap);
	}

	void sync(CsvDataSet dataSet) {
		// sync order is important => units before flows
		sourceSync.sync(dataSet);
		unitSync.sync(dataSet);
		flowSync.sync(dataSet);
		// TODO: make the parameter sync like the other syncs
		new GlobalParameterSync(dataSet, db).run();
	}

	public Source getSource(String key) {
		return sourceSync.sources().get(key);
	}

	/**
	 * Get the mapped quantity for the given unit symbol.
	 */
	public UnitMappingEntry quantityOf(String unit) {
		return unitSync.mapping().getEntry(unit);
	}
}
