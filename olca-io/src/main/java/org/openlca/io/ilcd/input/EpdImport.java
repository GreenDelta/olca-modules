package org.openlca.io.ilcd.input;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultImpact;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.ResultOrigin;
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

			// meta-data
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

			addRefFlow(result);
			addResultsOf(scope, result);
			config.db().insert(result);
		}
	}

	private void addRefFlow(ResultModel result) {
		var qRef = Processes.getQuantitativeReference(dataSet);
		if (qRef == null || qRef.referenceFlows.isEmpty())
			return;

		var exchange = dataSet.exchanges.stream()
			.filter(e -> qRef.referenceFlows.contains(e.id))
			.findAny()
			.orElse(null);
		if (exchange == null || exchange.flow == null)
			return;
		var f = FlowImport.get(config, exchange.flow.uuid);
		if (f.isEmpty())
			return;

		var resultFlow = new ResultFlow();
		resultFlow.flow = f.flow();
		resultFlow.isInput = f.flow().flowType == FlowType.WASTE_FLOW;
		resultFlow.flowPropertyFactor = f.property();
		resultFlow.unit = f.unit();
		resultFlow.origin = ResultOrigin.IMPORTED;

		double amount = exchange.resultingAmount != null
			? exchange.resultingAmount
			: exchange.meanAmount;
		if (f.isMapped() && f.mapFactor() != 0) {
			amount *= f.mapFactor();
		}
		resultFlow.amount = amount;
		result.inventory.add(resultFlow);
		result.referenceFlow = resultFlow;
	}

	private void addResultsOf(Scope scope, ResultModel result) {
		for (var r : epd.results) {
			if (r.indicator == null)
				continue;
			var amount = scope.amountOf(r);
			if (amount == null || amount.value == null)
				continue;
			var impact = impactOf(r.indicator);
			if (impact == null)
				continue;
			var ir = new ResultImpact();
			ir.origin = ResultOrigin.IMPORTED;
			ir.indicator = impact;
			ir.amount = amount.value;
			result.impacts.add(ir);
		}
	}

	private ImpactCategory impactOf(Indicator indicator) {
		if (indicator == null)
			return null;
		if (indicator.type == Indicator.Type.LCIA)
			return ImpactImport.get(config, indicator.uuid);

		// handle LCI indicators
		var refId = KeyGen.get("impact", indicator.uuid);
		var impact = config.db().get(ImpactCategory.class, refId);
		if (impact != null)
			return impact;
		var f = FlowImport.get(config, indicator.uuid);
		if (f.isEmpty())
			return null;

		// create an impact category for the LCI indicator
		impact = ImpactCategory.of(f.flow().name, indicator.unit);
		impact.refId = refId;
		impact.description = f.flow().description;
		double value = f.isMapped() && f.mapFactor() != 0
			? 1 / f.mapFactor()
			: 1;
		var factor = impact.factor(f.flow(), value);
		factor.flowPropertyFactor = f.property();
		factor.unit = f.unit();
		return config.db().insert(impact);
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
