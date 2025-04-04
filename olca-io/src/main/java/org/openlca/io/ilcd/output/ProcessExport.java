package org.openlca.io.ilcd.output;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Result;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ilcd.commons.Compliance;
import org.openlca.ilcd.commons.DataQualityIndicator;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.ImpactCategory;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.Quality;
import org.openlca.ilcd.commons.QualityIndicator;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.FlowCompletenessEntry;
import org.openlca.ilcd.processes.ImpactResult;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.ReviewMethod;
import org.openlca.ilcd.processes.ReviewScope;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.Sources;
import org.openlca.ilcd.util.TimeExtension;
import org.openlca.io.Xml;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The export of an openLCA process to an ILCD process data set.
 */
public class ProcessExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Export exp;
	private final org.openlca.core.model.Process proc;
	private ProcessDoc doc;

	// Optionally add the impact results of the process
	private Result result;
	private File flowChart;

	public ProcessExport(Export exp, org.openlca.core.model.Process proc) {
		this.exp = exp;
		this.proc = proc;
	}

	public ProcessExport withImpactResultsOf(Result result) {
		this.result = result;
		return this;
	}

	public ProcessExport withFlowChart(File flowChart) {
		this.flowChart = flowChart;
		return this;
	}

	public void write() {
		if (proc == null || exp.store.contains(Process.class, proc.refId))
			return;
		log.trace("Run process export with {}", proc);
		this.doc = proc.documentation;

		var ds = new Process();
		mapDataSetInfo(ds);
		mapTime(ds);
		mapGeography(ds);
		mapTechnology(ds);
		mapInventoryMethod(ds);
		mapRepresentativeness(ds);
		mapReviews(ds);
		mapCompleteness(ds);
		mapComplianceSystems(ds);

		ds.withAdminInfo(ProcessAdminInfo.create(exp, proc));
		var params = ProcessParameters.convert(exp, proc);
		if (!params.isEmpty()) {
			ds.withProcessInfo()
					.withParameterModel()
					.withParameters()
					.addAll(params);
		}

		Exchange qRef = proc.quantitativeReference;
		if (qRef != null) {
			ds.withProcessInfo()
					.withQuantitativeReference()
					.withType(QuantitativeReferenceType.REFERENCE_FLOWS)
					.withReferenceFlows()
					.add(qRef.internalId);
		}

		new ExchangeConversion(proc, exp).run(ds);
		addImpactResults(ds);

		exp.store.put(ds);
	}

	private void mapDataSetInfo(Process ds) {
		var info = ds.withProcessInfo()
				.withDataSetInfo()
				.withUUID(proc.refId);
		var name = info.withProcessName();
		exp.add(name::withBaseName, proc.name);
		exp.add(info::withComment, proc.description);
		Categories.toClassification(proc.category, info::withClassifications);
	}

	private void mapTime(Process ds) {
		if (doc == null)
			return;
		var time = ds.withProcessInfo().withTime();
		var ext = new TimeExtension(time);
		if (doc.validFrom != null) {
			time.withReferenceYear(Export.getYear(doc.validFrom));
			ext.setStartDate(doc.validFrom);
		}
		if (doc.validUntil != null) {
			time.withValidUntil(Export.getYear(doc.validUntil));
			ext.setEndDate(doc.validUntil);
		}
		exp.add(time::withDescription, doc.time);
	}

	private void mapGeography(Process ds) {
		if (doc == null)
			return;
		if (proc.location == null && doc.geography == null)
			return;
		var loc = ds.withProcessInfo()
				.withGeography()
				.withLocation();
		if (proc.location != null) {
			var oLoc = proc.location;
			loc.withCode(oLoc.code);
			// do not write (0.0, 0.0) locations; these are the default
			// location coordinates in openLCA but probably never a valid
			// process location, right?
			if (!(oLoc.latitude == 0.0 && oLoc.longitude == 0.0)) {
				loc.withLatitudeAndLongitude(oLoc.latitude + ";" + oLoc.longitude);
			}
		}
		exp.add(loc::withDescription, doc.geography);
	}

	private void mapTechnology(Process ds) {
		if (doc != null && Strings.notEmpty(doc.technology)) {
			var tech = ds.withProcessInfo().withTechnology();
			exp.add(tech::withDescription, doc.technology);
		}
		if (flowChart != null && flowChart.exists()) {
			var source = new Source();
			Sources.withUUID(source, UUID.randomUUID().toString());
			Sources.withVersion(source, "1.0");
			Sources.withTimeStamp(source, Xml.calendar(System.currentTimeMillis()));
			Sources.withName(source, LangString.of("Flow chart for " + proc.name));
			Sources.withDataSetInfo(source)
					.withFiles()
					.add(new FileRef().withUri("../external_docs/" + flowChart.getName()));
			exp.store.put(source, new File[]{flowChart});
			ds.withProcessInfo()
					.withTechnology()
					.withPictures()
					.add(Ref.of(source));
		}
	}

	private void mapInventoryMethod(Process ds) {
		var method = ds.withModelling().withInventoryMethod();
		if (proc.processType != null) {
			method.withProcessType(
					proc.processType == ProcessType.UNIT_PROCESS
							? org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX
							: org.openlca.ilcd.commons.ProcessType.LCI_RESULT);
		}
		method.withPrinciple(ModellingPrinciple.OTHER);
		if (doc != null) {
			exp.add(method::withPrincipleDeviations, doc.inventoryMethod);
			exp.add(method::withConstants, doc.modelingConstants);
		}
		var allocation = getAllocationMethod();
		if (allocation != null) {
			method.withApproaches().add(allocation);
		}
	}

	private ModellingApproach getAllocationMethod() {
		if (proc.defaultAllocationMethod == null)
			return null;
		return switch (proc.defaultAllocationMethod) {
			case CAUSAL -> ModellingApproach.ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT;
			case ECONOMIC -> ModellingApproach.ALLOCATION_MARKET_VALUE;
			case PHYSICAL -> ModellingApproach.ALLOCATION_PHYSICAL_CAUSALITY;
			default -> null;
		};
	}

	private void mapRepresentativeness(Process ds) {
		if (doc == null)
			return;
		var rep = ds.withModelling().withRepresentativeness();
		exp.add(rep::withCompleteness, doc.dataCompleteness);
		exp.add(rep::withDataSelection, doc.dataSelection);
		exp.add(rep::withDataTreatment, doc.dataTreatment);
		exp.add(rep::withSamplingProcedure, doc.samplingProcedure);
		exp.add(rep::withDataCollectionPeriod, doc.dataCollectionPeriod);
		exp.add(rep::withUseAdvice, doc.useAdvice);
		doc.sources.stream()
				.map(exp::writeRef)
				.filter(Objects::nonNull)
				.forEach(s -> rep.withSources().add(s));
	}

	private void mapReviews(Process ds) {
		if (doc == null || doc.reviews.isEmpty())
			return;
		var reviews = ds.withModelling()
				.withValidation()
				.withReviews();
		for (var r : doc.reviews) {
			var rev = new Review();
			reviews.add(rev);
			rev.withType(ReviewType.fromValue(r.type).orElse(null))
					.withReport(exp.writeRef(r.report));
			exp.add(rev::withDetails, r.details);
			for (var reviewer : r.reviewers) {
				var ref = exp.writeRef(reviewer);
				if (ref != null) {
					rev.withReviewers().add(ref);
				}
			}

			// review scopes & methods
			for (var s : r.scopes.values()) {
				var name = ReviewScope.fromValue(s.name).orElse(null);
				if (name == null)
					continue;
				var scope = new Review.Scope().withName(name);
				rev.withScopes().add(scope);
				for (var m : s.methods) {
					ReviewMethod.fromValue(m).ifPresent(method -> {
						var entry = new Review.Method().withName(method);
						scope.withMethods().add(entry);
					});
				}
			}

			// data quality assessment
			for (var dq : QualityIndicator.values()) {
				var a = r.assessment.get(dq.value());
				if (a == null)
					continue;
				var v = Quality.fromValue(a).orElse(null);
				if (v == null)
					continue;
				rev.withIndicators().add(
						new DataQualityIndicator()
								.withName(dq)
								.withValue(v));
			}
		}
	}

	private void mapCompleteness(Process ds) {
		if (doc == null || doc.flowCompleteness.isEmpty())
			return;
		var c = doc.flowCompleteness;
		var productAspect = FlowCompleteness
				.fromValue(c.get("Product model"))
				.orElse(null);
		var completeness = ds.withModelling()
				.withCompleteness()
				.withProductCompleteness(productAspect);
		for (var impact : ImpactCategory.values()) {
			var aspect = FlowCompleteness
					.fromValue(c.get(impact.value()))
					.orElse(null);
			if (aspect == null)
				continue;
			var e = new FlowCompletenessEntry()
					.withImpact(impact)
					.withValue(aspect);
			completeness.withEntries().add(e);
		}
	}

	private void mapComplianceSystems(Process ds) {
		if (doc == null || doc.complianceDeclarations.isEmpty())
			return;
		var decs = ds.withModelling().withComplianceDeclarations();
		for (var c : doc.complianceDeclarations) {
			var dec = new ComplianceDeclaration()
					.withSystem(exp.writeRef(c.system));
			Compliance.fromValue(c.aspects.get("Overall compliance"))
					.ifPresent(dec::withApproval);
			Compliance.fromValue(c.aspects.get("Nomenclature compliance"))
					.ifPresent(dec::withNomenclature);
			Compliance.fromValue(c.aspects.get("Methodological compliance"))
					.ifPresent(dec::withMethod);
			Compliance.fromValue(c.aspects.get("Review compliance"))
					.ifPresent(dec::withReview);
			Compliance.fromValue(c.aspects.get("Documentation compliance"))
					.ifPresent(dec::withDocumentation);
			Compliance.fromValue(c.aspects.get("Quality compliance"))
					.ifPresent(dec::withQuality);
			decs.add(dec);
		}
	}

	private void addImpactResults(Process ds) {
		if (result == null || result.impactResults.isEmpty())
			return;
		for (var impact : result.impactResults) {
			if (impact.indicator == null)
				continue;
			var ref = exp.writeRef(impact.indicator);
			var r = new ImpactResult()
					.withMethod(ref)
					.withAmount(impact.amount);
			exp.add(r::withComment, impact.description);
			ds.withImpactResults().add(r);
		}
	}

}
