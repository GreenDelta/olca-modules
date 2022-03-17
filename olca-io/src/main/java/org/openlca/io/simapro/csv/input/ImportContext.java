package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.RootEntity;
import org.openlca.io.maps.FlowMap;
import org.openlca.simapro.csv.CsvDataSet;

record ImportContext(
	IDatabase db,
	RefData refData,
	ImportLog log,
	CsvDataSet dataSet
) {

	ImportContext(Builder b, CsvDataSet dataSet) {
		this(b.db, b.refData, b.log, dataSet);
	}

	public <T extends RootEntity> T insert(T entity) {
		var e = db.insert(entity);
		log.imported(e);
		return e;
	}

	static Builder of(IDatabase db, FlowMap flowMap, ImportLog log) {
		var refData = new RefData(db, flowMap, log);
		return new Builder(db, refData, log);
	}

	record Builder(IDatabase db, RefData refData, ImportLog log) {

		ImportContext next(CsvDataSet dataSet) {
			refData.sync(dataSet);
			return new ImportContext(this, dataSet);
		}
	}

}
