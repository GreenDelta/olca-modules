package org.openlca.io.simapro.csv.input;

import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Source;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.MapFactor;
import org.openlca.simapro.csv.CsvDataSet;

class RefData {

	private final IDatabase db;
	private final UnitSync unitSync;
	private final SourceSync sourceSync;

	RefData(IDatabase db) {
		this.db = db;
		this.unitSync = new UnitSync(db);
		this.sourceSync = new SourceSync(db);
	}

	void sync(CsvDataSet dataSet) {
		// sync order is important => units before flows
		unitSync.sync(dataSet);
		sourceSync.sync(dataSet);
		// TODO: make the parameter sync like the other syncs
		new GlobalParameterSync(dataSet, db).run();
	}

	public Source getSource(String key) {
		return sourceSync.sources().get(key);
	}

	public UnitMappingEntry getUnit(String unit) {
		return unitSync.mapping().getEntry(unit);
	}

	public void putProduct(String key, Flow flow) {
		products.put(key, flow);
	}

	public Flow getProduct(String key) {
		return products.get(key);
	}

	public void putElemFlow(String key, Flow flow) {
		elemFlows.put(key, flow);
	}

	public Flow getElemFlow(String key) {
		return elemFlows.get(key);
	}

	public void putMappedFlow(String key, MapFactor<Flow> factor) {
		mappedFlows.put(key, factor);
	}

	public MapFactor<Flow> getMappedFlow(String key) {
		return mappedFlows.get(key);
	}



}
