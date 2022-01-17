package org.openlca.io.ilcd.input;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultImpact;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.ResultOrigin;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.epd.conversion.EpdExtensions;
import org.openlca.ilcd.epd.model.Amount;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.Indicator;
import org.openlca.ilcd.epd.model.IndicatorResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

public record EpdImport(ImportConfig config, Process dataSet, EpdDataSet epd) {

	public EpdImport(ImportConfig config, Process dataSet) {
		this(config, dataSet, EpdExtensions.read(dataSet));
	}

	public void run() {
		for (var scope : Scope.allOf(epd)) {
			var suffix = scope.toString();

			var refId = KeyGen.get(dataSet.getUUID(), suffix);
			var result = config.db().get(ResultModel.class, refId);
			if (result != null)
				continue;
			result = new ResultModel();
			result.refId = refId;
			result.urn = "ilcd:epd:" + dataSet.getUUID();

			result.name = Strings.cut(
				Processes.fullName(dataSet, config.langOrder()),
				2044 - suffix.length()) + " - " + suffix;
			config.log().info("import EPD result: " + result.name);
			result.category = new CategoryDao(config.db())
				.sync(ModelType.RESULT, Categories.getPath(dataSet));
			var info = Processes.getDataSetInfo(dataSet);
			if (info != null) {
				result.description = config.str(info.comment);
			}

			addResultsOf(scope, result);
			config.db().insert(result);
		}

	}

	private void addResultsOf(Scope scope, ResultModel result) {
		for (var r : epd.results) {
			if (r.indicator == null)
				continue;
			var amount = scope.amountOf(r);
			if (amount == null || amount.value == null)
				continue;
			var impact = impactOf(r);
			if (impact == null)
				continue;
			var ir = new ResultImpact();
			ir.origin = ResultOrigin.IMPORTED;
			ir.indicator = impact;
			ir.amount = amount.value;
			result.impacts.add(ir);




			if (r.indicator.type == Indicator.Type.LCI) {
				var flow = FlowImport.get(config, r.indicator.uuid);
				if (flow == null)
					continue;
				var impact = impactOf(flow);

			}


			if (r.indicator)

		}
	}

	private ImpactCategory impactOf(IndicatorResult r) {

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

	private record Scope(String module, String scenario) {

		static Set<Scope> allOf(EpdDataSet epd) {
			var scopes = new HashSet<Scope>();
			for (var result : epd.results) {
				for (var amount : result.amounts) {
					if (amount.module == null || amount.module.name == null)
						continue;
					scopes.add(new Scope(amount.module.name, amount.scenario));
				}
			}
			return scopes;
		}

		Amount amountOf(IndicatorResult result) {
			if (result == null)
				return null;
			for (var a : result.amounts) {
				if (a.module == null)
					continue;
				if (Objects.equals(a.module.name, module)
					&& Objects.equals(a.scenario, scenario)) {
					return a;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			if (module == null)
				return "";
			return scenario != null
				? module + " - " + scenario
				: module;
		}
	}
}
