package org.openlca.io.ilcd.input;

import java.util.Objects;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultImpact;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.ResultOrigin;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.Strings;

public record EpdImport(ImportConfig config, Process dataSet) {

	public ResultModel run() {
		var result = config.db().get(ResultModel.class, dataSet.getUUID());
		return result != null
			? result
			: createNew();
	}

	private ResultModel createNew() {
		var result = new ResultModel();
		result.refId = dataSet.getUUID();
		result.name = Strings.cut(
			Processes.fullName(dataSet, config.langOrder()), 2024);
		config.log().info("import EPD: " + result.name);
		result.category = new CategoryDao(config.db())
			.sync(ModelType.RESULT, Categories.getPath(dataSet));

		var info = Processes.getDataSetInfo(dataSet);
		if (info != null) {
			result.description = config.str(info.comment);
		}

		mapFlows(result);
		mapImpacts(result);

		return config.db().insert(result);
	}

	private void mapFlows(ResultModel result) {
		var index = new TIntObjectHashMap<ResultFlow>();
		for (var e : dataSet.exchanges) {
			if (e.flow == null || e.flow.uuid == null)
				continue;
			var syncFlow = FlowImport.get(config, e.flow.uuid);
			if (syncFlow.isEmpty()) {
				config.log().warn("EPD '" + result.refId
					+ "' contains invalid flow references");
				continue;
			}

			var resultFlow = new ResultFlow();
			resultFlow.flow = syncFlow.flow();
			resultFlow.flowPropertyFactor = syncFlow.property();
			resultFlow.unit = syncFlow.unit();
			resultFlow.amount = e.resultingAmount == null
				? e.meanAmount
				: e.resultingAmount;
			resultFlow.isInput = e.direction == ExchangeDirection.INPUT;
			resultFlow.description = config.str(e.comment);
			resultFlow.location = config.locationOf(e.location);
			resultFlow.origin = ResultOrigin.IMPORTED;
			result.inventory.add(resultFlow);
			index.put(e.id, resultFlow);
		}

		var qRef = Processes.getQuantitativeReference(dataSet);
		if (qRef != null) {
			result.referenceFlow = qRef.referenceFlows.stream()
				.filter(Objects::nonNull)
				.map(index::get)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
		}
	}

	private void mapImpacts(ResultModel result) {
		if (dataSet.lciaResults == null)
			return;
		for (var r : dataSet.lciaResults) {
			if (r.method == null || r.method.uuid == null)
				continue;
			var impact = ImpactImport.get(config, r.method.uuid);
			if (impact == null) {
				config.log().warn("EPD '" + result.refId
					+ "' contains invalid impact category references");
				continue;
			}

			var resultImpact = new ResultImpact();
			resultImpact.indicator = impact;
			resultImpact.amount = r.amount;
			resultImpact.description = config().str(r.comment);
			resultImpact.origin = ResultOrigin.IMPORTED;
			result.impacts.add(resultImpact);
		}
	}
}
