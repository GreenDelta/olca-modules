package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Source;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.SyncFlow;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.method.ImpactFactorRow;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ExchangeRow;

class RefData {

	private final UnitSync unitSync;
	private final SourceSync sourceSync;
	private final CsvFlowSync flowSync;
	private final GlobalParameterSync parameterSync;

	RefData(IDatabase db, FlowMap flowMap, ImportLog log) {
		this.unitSync = new UnitSync(db, log);
		this.sourceSync = new SourceSync(db, log);
		this.parameterSync = new GlobalParameterSync(db, log);
		this.flowSync = new CsvFlowSync(db, this, flowMap, log);
	}

	void sync(CsvDataSet dataSet) {
		// sync order is important => units before flows
		sourceSync.sync(dataSet);
		unitSync.sync(dataSet);
		flowSync.sync(dataSet);
		parameterSync.sync(dataSet);
	}

	Source sourceOf(String name) {
		return sourceSync.sources().get(name);
	}

	/**
	 * Get the mapped quantity for the given unit symbol.
	 */
	UnitMappingEntry quantityOf(String unit) {
		return unitSync.mapping().getEntry(unit);
	}

	SyncFlow elemFlowOf(ElementaryFlowType type, ElementaryExchangeRow row) {
		return flowSync.elemFlow(type, row);
	}

	SyncFlow elemFlowOf(ImpactFactorRow row) {
		return flowSync.elemFlow(row);
	}

	SyncFlow wasteFlowOf(ExchangeRow row) {
		return flowSync.waste(row);
	}

	SyncFlow productOf(ExchangeRow row) {
		return flowSync.product(row);
	}
}
