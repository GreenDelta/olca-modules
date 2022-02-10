package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.CsvDataSet;

class ImportContext {

	private final IDatabase db;
	private final RefData refData;
	private final CsvDataSet dataSet;

	private ImportContext(Builder builder, CsvDataSet dataSet) {
		this.db = builder.db;
		this.refData = builder.refData;
		this.dataSet = dataSet;
	}

	IDatabase db() {
		return db;
	}

	RefData refData() {
		return refData;
	}

	public CsvDataSet dataSet() {
		return dataSet;
	}

	static Builder of(IDatabase db, FlowMap flowMap, ImportLog log) {
		var refData = new RefData(db, flowMap);
		return new Builder(db, refData);
	}

	record Builder (IDatabase db, RefData refData) {

		ImportContext next(CsvDataSet dataSet) {
			refData.sync(dataSet);
			return new ImportContext(this, dataSet);
		}
	}

}
