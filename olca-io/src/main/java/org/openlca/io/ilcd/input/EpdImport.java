package org.openlca.io.ilcd.input;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.EpdType;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.epd.EpdValue;
import org.openlca.ilcd.util.Epds;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.KeyGen;
import org.openlca.util.Lists;
import org.openlca.util.Strings;

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
		oEpd.refId = id;
		oEpd.name = Strings.cutEnd(
				Processes.getFullName(ds, imp.lang()), 2048);
		oEpd.category = imp.syncCategory(ds, ModelType.EPD);
		oEpd.tags = tags();
		Import.mapVersionInfo(ds, oEpd);
		mapEpdMetaData(oEpd);

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

		Category resultCategory = null;
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
			result.version = oEpd.version;
			result.lastChange = oEpd.lastChange;

			// result meta-data
			result.name = Strings.cutEnd(
					Processes.getFullName(ds, imp.lang()),
					2044 - suffix.length()) + " - " + suffix;
			result.category = resultCategory == null
					? (resultCategory = resultCategoryOf(ds, oEpd.name))
					: resultCategory;

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

	private Category resultCategoryOf(IDataSet ds, String epdName) {
		var base = imp.categoryPathOf(ds);
		if (base == null && Strings.nullOrEmpty(epdName))
			return null;
		if (Strings.nullOrEmpty(epdName))
			return CategoryDao.sync(imp.db(), ModelType.RESULT, base);
		var last = epdName.replace('/', '|');
		if (base == null)
			return CategoryDao.sync(imp.db(), ModelType.RESULT, last);
		var path = new String[base.length + 1];
		System.arraycopy(base, 0, path, 0, base.length);
		path[base.length] = last;
		return CategoryDao.sync(imp.db(), ModelType.RESULT, path);
	}

	private void mapEpdMetaData(Epd oEpd) {
		var info = Processes.getDataSetInfo(ds);
		if (info != null) {
			oEpd.description = imp.str(info.getComment());
		}
		oEpd.verifier = verifier();
		oEpd.programOperator = operator();
		oEpd.manufacturer = manufacturer();
		oEpd.pcr = pcr();

		// EPD type
		var iType = Epds.getSubType(ds);
		if (iType != null) {
			oEpd.epdType = switch (iType) {
				case AVERAGE_DATASET -> EpdType.AVERAGE_DATASET;
				case GENERIC_DATASET -> EpdType.GENERIC_DATASET;
				case SPECIFIC_DATASET -> EpdType.SPECIFIC_DATASET;
				case TEMPLATE_DATASET -> EpdType.TEMPLATE_DATASET;
				case REPRESENTATIVE_DATASET -> EpdType.REPRESENTATIVE_DATASET;
			};
		}

		// time
		var iTime = Epds.getTime(ds);
		var pubDate = Epds.getPublicationDate(ds);
		oEpd.validFrom = pubDate != null
				? pubDate.toGregorianCalendar().getTime()
				: ProcessTime.validFrom(iTime).orElse(null);
		oEpd.validUntil = ProcessTime.validUntil(iTime).orElse(null);

		// tech. description
		var iTech = Epds.getTechnology(ds);
		if (iTech != null) {
			oEpd.productUsage = imp.str(iTech.getApplicability());
			oEpd.manufacturing = imp.str(iTech.getDescription());
		}

		// location
		var iLoc = Epds.getLocation(ds);
		if (iLoc != null) {
			oEpd.location = imp.cache.locationOf(iLoc.getCode());
		}

		// use advice & original EPD
		var iRepr = Epds.getRepresentativeness(ds);
		if (iRepr != null) {
			oEpd.useAdvice = imp.str(iRepr.getUseAdvice());

			var ext = iRepr.getEpdExtension();
			if (ext != null) {
				Lists.first(ext.getOriginalEpds()).ifPresent(ref ->
						oEpd.originalEpd = SourceImport.get(imp, ref.getUUID())
				);
			}
		}

		// registration ID
		var iPub = Epds.getPublication(ds);
		if (iPub != null) {
			oEpd.registrationId = iPub.getRegistrationNumber();
		}

		// data generator
		var iGen = Epds.getDataGenerator(ds);
		if (iGen != null) {
			Lists.first(iGen.getContacts()).ifPresent(ref ->
					oEpd.dataGenerator = ContactImport.get(imp, ref.getUUID()));
		}
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
			var impact = imp.resolveIndicatorOf(r);
			if (impact == null)
				continue;
			var ir = new ImpactResult();
			ir.indicator = impact;
			ir.amount = v.getAmount();
			result.impactResults.add(ir);
		}
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

	private Source pcr() {
		var m = Epds.getInventoryMethod(ds);
		if (m == null)
			return null;
		var ref = Lists.first(m.getSources()).orElse(null);
		if (ref == null)
			return null;
		return SourceImport.get(imp, ref.getUUID());
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

		EpdValue valueOf(EpdIndicatorResult result) {
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
				return "unknown";
			return scenario != null
					? module + " - " + scenario
					: module;
		}
	}
}
