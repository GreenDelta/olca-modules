package org.openlca.io.simapro.csv.input;

import java.util.Objects;
import java.util.Optional;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.RootEntity;
import org.openlca.io.maps.SyncFlow;
import org.openlca.io.simapro.csv.input.EIProviderResolver.Provider;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.process.ExchangeRow;

record ImportContext(
		IDatabase db,
		RefData refData,
		ImportLog log,
		EIProviderResolver providers,
		CsvDataSet dataSet
) {

	ImportContext(Builder b, CsvDataSet dataSet) {
		this(b.db, b.refData, b.log, b.providers, dataSet);
	}

	public <T extends RootEntity> T insert(T entity) {
		var e = db.insert(entity);
		log.imported(e);
		return e;
	}

	Optional<Provider> resolveProvider(String name) {
		return providers != null
				? providers.resolve(name)
				: Optional.empty();
	}

	Optional<SyncFlow> resolveProviderFlow(ExchangeRow row) {
		if (row == null)
			return Optional.empty();
		var p = resolveProvider(row.name()).orElse(null);
		if (p == null)
			return Optional.empty();
		var unit = refData.quantityOf(row.unit());
		if (unit == null)
			return Optional.empty();

		var flow = db.get(Flow.class, p.techFlow().flowId());
		if (flow == null)
			return Optional.empty();

		FlowPropertyFactor factor = null;
		for (var fac : flow.flowPropertyFactors) {
			if (Objects.equals(fac.flowProperty, unit.flowProperty)) {
				factor = fac;
				break;
			}
		}
		if (factor == null)
			return Optional.empty();

		var process = p.techFlow().isProcess()
				? p.techFlow().provider()
				: null;

		var syncFlow = new SyncFlow(flow, factor, unit.unit, process, false, 1);
		return Optional.of(syncFlow);
	}

	static Builder of(
			IDatabase db,
			FlowMap flowMap,
			ImportLog log,
			EIProviderResolver providers
	) {
		var refData = new RefData(db, flowMap, log);
		return new Builder(db, refData, log, providers);
	}

	record Builder(
			IDatabase db,
			RefData refData,
			ImportLog log,
			EIProviderResolver providers
	) {

		ImportContext next(CsvDataSet dataSet) {
			refData.sync(dataSet);
			return new ImportContext(this, dataSet);
		}
	}

}
