package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdResult;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.EpdIndicatorResult;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.KeyGen;
import org.openlca.util.Lists;
import org.openlca.util.Strings;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class EpdImport {

	private final Import imp;
	private final Process ds;
	private final List<EpdIndicatorResult> results;
	private final AtomicBoolean hasRefError;

	public EpdImport(Import imp, Process ds) {
		this.imp = imp;
		this.ds = ds;
		this.results = EpdIndicatorResult.allOf(ds);
		this.hasRefError = new AtomicBoolean(false);
	}

	public void run() {
		var id = Processes.getUUID(ds);
		var oEpd = imp.db().get(Epd.class, id);
		if (oEpd != null) {
			imp.log().skipped(oEpd);
			return;
		}

		oEpd = new Epd();
		oEpd.urn = "ilcd:epd:" + id;
		oEpd.refId = id;
		oEpd.lastChange = System.currentTimeMillis();
		oEpd.name = Strings.cut(
				Processes.getFullName(ds, imp.langOrder()), 2048);
		var path = Categories.getPath(ds);
		oEpd.category = new CategoryDao(imp.db()).sync(ModelType.EPD, path);
		oEpd.tags = tags();
		Import.mapVersionInfo(ds, oEpd);

		var info = Processes.getDataSetInfo(ds);
		if (info != null) {
			oEpd.description = imp.str(info.getComment());
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

		for (var scope : Scope.allOf(results)) {
			var suffix = scope.toString();

			var refId = KeyGen.get(id, suffix);
			var result = imp.db().get(Result.class, refId);
			if (result != null) {
				var module = EpdModule.of(scope.toString(), result);
				oEpd.modules.add(module);
				imp.log().skipped(result);
				continue;
			}

			result = new Result();
			result.refId = refId;
			result.tags = suffix;

			// meta-data
			result.name = Strings.cut(
					Processes.getFullName(ds, imp.langOrder()),
					2044 - suffix.length()) + " - " + suffix;
			imp.log().info("import EPD result: " + result.name);
			result.category = new CategoryDao(imp.db())
					.sync(ModelType.RESULT, path);

			if (refFlow != null) {
				var resultRef = refFlow.copy();
				result.referenceFlow = resultRef;
				result.flowResults.add(resultRef);
			}

			addResultsOf(scope, result);
			result = imp.insert(result);
			oEpd.modules.add(EpdModule.of(scope.toString(), result));
		}

		imp.insert(oEpd);
	}

	private FlowResult getRefFlow() {
		var qRef = Processes.getQuantitativeReference(ds);
		if (qRef == null || qRef.getReferenceFlows().isEmpty())
			return null;

		var exchange = ds.getExchanges().stream()
				.filter(e -> qRef.getReferenceFlows().contains(e.getId()))
				.findAny()
				.orElse(null);
		if (exchange == null || exchange.getFlow() == null)
			return null;

		var flowId = exchange.getFlow().getUUID();
		var f = FlowImport.get(imp, flowId);
		if (f.isEmpty()) {
			if (!hasRefError.get()) {
				hasRefError.set(true);
				imp.log().error("EPD " + Processes.getUUID(ds)
						+ " has invalid references; e.g. flow: " + flowId);
			}
			return null;
		}

		var ref = new FlowResult();
		ref.flow = f.flow();
		ref.isInput = f.flow().flowType == FlowType.WASTE_FLOW;
		ref.flowPropertyFactor = f.property();
		ref.unit = f.unit();

		double amount = exchange.getResultingAmount() != null
				? exchange.getResultingAmount()
				: exchange.getMeanAmount();
		if (f.isMapped() && f.mapFactor() != 0) {
			amount *= f.mapFactor();
		}
		ref.amount = amount;

		return ref;
	}

	private void addResultsOf(Scope scope, Result result) {
		for (var r : results) {
			if (r.indicator() == null)
				continue;
			var v = scope.valueOf(r);
			if (v == null || v.getAmount() == null)
				continue;
			var impact = impactOf(r);
			if (impact == null)
				continue;
			var ir = new ImpactResult();
			ir.indicator = impact;
			ir.amount = v.getAmount();
			result.impactResults.add(ir);
		}
	}

	private ImpactCategory impactOf(EpdIndicatorResult r) {
		if (r.indicator() == null || !r.indicator().isValid())
			return null;

		String unit = null;
		if (r.unitGroup() != null) {
			var u = LangString.getFirst(r.unitGroup().getName());
			if (Strings.notEmpty(u)) {
				unit = u;
			}
		}

		// handle LCIA indicators
		if (r.hasImpactIndicator()) {
			var impact = ImpactImport.get(imp, r.indicator().getUUID());

			// found an impact
			if (impact != null) {
				if (Strings.nullOrEmpty(impact.referenceUnit)
						&& unit != null) {
					// indicator units are sometimes missing in
					// LCIA data sets of ILCD packages
					impact.referenceUnit = unit;
					imp.db().update(impact);
				}
				return impact;
			}

			// create a new impact category
			var name = LangString.getFirst(r.indicator().getName());
			impact = ImpactCategory.of(name, unit);
			impact.refId = r.indicator().getUUID();
			return imp.db().insert(impact);
		}

		// handle LCI indicators
		var refId = KeyGen.get("impact", r.indicator().getUUID());
		var impact = imp.db().get(ImpactCategory.class, refId);
		if (impact != null)
			return impact;
		var name = LangString.getFirst(r.indicator().getName());
		impact = ImpactCategory.of(name, unit);
		impact.refId = refId;
		var f = FlowImport.get(imp, r.indicator().getUUID());
		if (f.isEmpty()) {
			return imp.db().insert(impact);
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
		return imp.db().insert(impact);
	}

	private Actor operator() {
		var pub = Processes.getPublication(ds);
		if (pub == null || pub.getRegistrationAuthority() == null)
			return null;
		var id = pub.getRegistrationAuthority().getUUID();
		return ContactImport.get(imp, id);
	}

	private Actor manufacturer() {
		var pub = Processes.getPublication(ds);
		if (pub == null || pub.getOwner() == null)
			return null;
		var id = pub.getOwner().getUUID();
		return ContactImport.get(imp, id);
	}

	private Actor verifier() {
		var v = Processes.getValidation(ds);
		if (v == null)
			return null;
		var review = Lists.first(v.getReviews()).orElse(null);
		if (review == null)
			return null;
		var ref = Lists.first(review.getReviewers()).orElse(null);
		if (ref == null)
			return null;
		return ContactImport.get(imp, ref.getUUID());
	}

	private String tags() {
		var decls = Processes.getComplianceDeclarations(ds);
		if (Lists.isEmpty(decls))
			return null;
		var tags = new StringBuilder();
		for (var decl : decls) {
			if (decl.getSystem() == null)
				continue;
			var sys = imp.str(decl.getSystem().getName());
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

		static Set<Scope> allOf(List<EpdIndicatorResult> results) {
			var scopes = new HashSet<Scope>();
			for (var result : results) {
				for (var v : result.values()) {
					if (Strings.nullOrEmpty(v.getModule()))
						continue;
					scopes.add(new Scope(v.getModule(), v.getScenario()));
				}
			}
			return scopes;
		}

		EpdResult valueOf(EpdIndicatorResult result) {
			if (result == null)
				return null;
			for (var a : result.values()) {
				if (Objects.equals(a.getModule(), module)
						&& Objects.equals(a.getScenario(), scenario)) {
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
