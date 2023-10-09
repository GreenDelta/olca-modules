package org.openlca.io.ilcd.input;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.*;
import org.openlca.ilcd.epd.conversion.EpdExtensions;
import org.openlca.ilcd.epd.model.Amount;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.Indicator;
import org.openlca.ilcd.epd.model.IndicatorResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.KeyGen;
import org.openlca.util.Lists;
import org.openlca.util.Strings;

public class EpdImport {

	private final ImportConfig config;
	private final Process dataSet;
	private final EpdDataSet epd;
	private final AtomicBoolean hasRefError;

	public EpdImport(ImportConfig config, Process dataSet) {
		this.config = config;
		this.dataSet = dataSet;
		this.epd = EpdExtensions.read(dataSet);
		this.hasRefError = new AtomicBoolean(false);
	}

	public void run() {
		var oEpd = config.db().get(Epd.class, dataSet.getUUID());
		if (oEpd != null) {
			config.log().skipped(oEpd);
			return;
		}

		oEpd = new Epd();
		oEpd.urn = "ilcd:epd:" + dataSet.getUUID();
		oEpd.refId = dataSet.getUUID();
		oEpd.lastChange = System.currentTimeMillis();
		oEpd.name = Strings.cut(
			Processes.fullName(dataSet, config.langOrder()), 2048);
		var path = Categories.getPath(dataSet);
		oEpd.category = new CategoryDao(config.db()).sync(ModelType.EPD, path);
		oEpd.tags = tags();

		var info = Processes.getDataSetInfo(dataSet);
		if (info != null) {
			oEpd.description = config.str(info.comment);
		}
		oEpd.verifier = verifier();
		oEpd.programOperator = operator();
		oEpd.manufacturer = manufacturer();

		// declared product
		var refFlow = getRefFlow();
		if (refFlow != null) {
			oEpd.product = new EpdProduct();
			oEpd.product.flow = refFlow.flow;
			oEpd.product.property = refFlow.flowPropertyFactor != null
				? refFlow.flowPropertyFactor.flowProperty
				: null;
			oEpd.product.unit = refFlow.unit;
			oEpd.product.amount = refFlow.amount;
		}

		for (var scope : Scope.allOf(epd)) {
			var suffix = scope.toString();

			var refId = KeyGen.get(dataSet.getUUID(), suffix);
			var result = config.db().get(Result.class, refId);
			if (result != null) {
				var module = EpdModule.of(scope.toString(), result);
				oEpd.modules.add(module);
				config.log().skipped(result);
				continue;
			}

			result = new Result();
			result.refId = refId;
			result.tags = suffix;

			// meta-data
			result.name = Strings.cut(
				Processes.fullName(dataSet, config.langOrder()),
				2044 - suffix.length()) + " - " + suffix;
			config.log().info("import EPD result: " + result.name);
			result.category = new CategoryDao(config.db())
				.sync(ModelType.RESULT, path);

			if (refFlow != null) {
				var resultRef = refFlow.copy();
				result.referenceFlow = resultRef;
				result.flowResults.add(resultRef);
			}

			addResultsOf(scope, result);
			result = config.insert(result);
			oEpd.modules.add(EpdModule.of(scope.toString(), result));
		}

		config.insert(oEpd);
	}

	private FlowResult getRefFlow() {
		var qRef = Processes.getQuantitativeReference(dataSet);
		if (qRef == null || qRef.referenceFlows.isEmpty())
			return null;

		var exchange = dataSet.exchanges.stream()
			.filter(e -> qRef.referenceFlows.contains(e.id))
			.findAny()
			.orElse(null);
		if (exchange == null || exchange.flow == null)
			return null;
		var f = FlowImport.get(config, exchange.flow.uuid);
		if (f.isEmpty()) {
			if (!hasRefError.get()) {
				hasRefError.set(true);
				config.log().error("EPD " + dataSet.getUUID()
						+ " has invalid references; e.g. flow: " + exchange.flow.uuid);
			}
			return null;
		}

		var ref = new FlowResult();
		ref.flow = f.flow();
		ref.isInput = f.flow().flowType == FlowType.WASTE_FLOW;
		ref.flowPropertyFactor = f.property();
		ref.unit = f.unit();

		double amount = exchange.resultingAmount != null
			? exchange.resultingAmount
			: exchange.meanAmount;
		if (f.isMapped() && f.mapFactor() != 0) {
			amount *= f.mapFactor();
		}
		ref.amount = amount;

		return ref;
	}

	private void addResultsOf(Scope scope, Result result) {
		for (var r : epd.results) {
			if (r.indicator == null)
				continue;
			var amount = scope.amountOf(r);
			if (amount == null || amount.value == null)
				continue;
			var impact = impactOf(r.indicator);
			if (impact == null)
				continue;
			var ir = new ImpactResult();
			ir.indicator = impact;
			ir.amount = amount.value;
			result.impactResults.add(ir);
		}
	}

	private ImpactCategory impactOf(Indicator indicator) {
		if (indicator == null)
			return null;

		// handle LCIA indicators
		if (indicator.type == Indicator.Type.LCIA) {
			var impact = ImpactImport.get(config, indicator.uuid);

			// found an impact
			if (impact != null) {
				if (Strings.nullOrEmpty(impact.referenceUnit)
					&& Strings.notEmpty(indicator.unit)) {
					// indicator units are sometimes missing in
					// LCIA data sets of ILCD packages
					impact.referenceUnit = indicator.unit;
					config.db().update(impact);
				}
				return impact;
			}

			// create a new impact category
			impact = ImpactCategory.of(indicator.name, indicator.unit);
			impact.refId = indicator.uuid;
			return config.db().insert(impact);
		}

		// handle LCI indicators
		var refId = KeyGen.get("impact", indicator.uuid);
		var impact = config.db().get(ImpactCategory.class, refId);
		if (impact != null)
			return impact;
		impact = ImpactCategory.of(indicator.name, indicator.unit);
		impact.refId = refId;
		var f = FlowImport.get(config, indicator.uuid);
		if (f.isEmpty()) {
			return config.db().insert(impact);
		}

		// add a factor for the ILCD+EPD flow
		impact.name = f.flow().name;
		impact.description = f.flow().description;
		double value = f.isMapped() && f.mapFactor() != 0
			? 1 / f.mapFactor()
			: 1;
		var factor = impact.factor(f.flow(), value);
		factor.flowPropertyFactor = f.property();
		factor.unit = f.unit();
		return config.db().insert(impact);
	}

	private Actor operator() {
		var pub = Processes.getPublication(dataSet);
		if (pub == null || pub.registrationAuthority == null)
			return null;
		var id = pub.registrationAuthority.uuid;
		return ContactImport.get(config, id);
	}

	private Actor manufacturer() {
		var pub = Processes.getPublication(dataSet);
		if (pub == null || pub.owner == null)
			return null;
		var id = pub.owner.uuid;
		return ContactImport.get(config, id);
	}

	private Actor verifier() {
		var v = Processes.getValidation(dataSet);
		if (v == null)
			return null;
		var review = Lists.first(v.reviews).orElse(null);
		if (review == null)
			return null;
		var ref = Lists.first(review.reviewers).orElse(null);
		if (ref == null)
			return null;
		return ContactImport.get(config, ref.uuid);
	}

	private String tags() {
		var decls = Processes.getComplianceDeclarations(dataSet);
		if (Lists.isEmpty(decls))
			return null;
		var tags = new StringBuilder();
		for (var decl : decls) {
			if (decl.system == null)
				continue;
			var sys = config.str(decl.system.name);
			if (Strings.nullOrEmpty(sys))
				continue;
			if (!tags.isEmpty()) {
				tags.append(',');
			}
			tags.append(sys);
		}
		return tags.isEmpty() ? null : tags.toString();
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
