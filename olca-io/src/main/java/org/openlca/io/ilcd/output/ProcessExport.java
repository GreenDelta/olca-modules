package org.openlca.io.ilcd.output;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ilcd.commons.Compliance;
import org.openlca.ilcd.commons.DataQualityIndicator;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.ImpactCategory;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.Quality;
import org.openlca.ilcd.commons.QualityIndicator;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.FlowCompletenessEntry;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.ReviewMethod;
import org.openlca.ilcd.processes.ReviewScope;
import org.openlca.ilcd.util.TimeExtension;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The export of an openLCA process to an ILCD process data set.
 */
public class ProcessExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Export exp;
	private org.openlca.core.model.Process process;
	private ProcessDoc doc;

	public ProcessExport(Export exp) {
		this.exp = exp;
	}

	public void write(org.openlca.core.model.Process p) {
		if (p == null || exp.store.contains(Process.class, p.refId))
			return;
		log.trace("Run process export with {}", p);
		this.process = p;
		this.doc = p.documentation;

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

		ds.withAdminInfo(new ProcessAdminInfo(exp).create(p));
		var params = new ProcessParameterConversion(exp).run(p);
		if (!params.isEmpty()) {
			ds.withProcessInfo()
					.withParameterModel()
					.withParameters()
					.addAll(params);
		}

		Exchange qRef = p.quantitativeReference;
		if (qRef != null) {
			ds.withProcessInfo()
					.withQuantitativeReference()
					.withType(QuantitativeReferenceType.REFERENCE_FLOWS)
					.withReferenceFlows()
					.add(qRef.internalId);
		}

		new ExchangeConversion(p, exp).run(ds);
		exp.store.put(ds);
	}

	private void mapDataSetInfo(Process ds) {
		var info = ds.withProcessInfo()
				.withDataSetInfo()
				.withUUID(process.refId);
		var name = info.withProcessName();
		exp.add(name::withBaseName, process.name);
		exp.add(info::withComment, process.description);
		Categories.toClassification(process.category, info::withClassifications);
	}

	private void mapTime(Process ds) {
		if (doc == null)
			return;
		var time = ds.withProcessInfo().withTime();
		var ext = new TimeExtension(time);
		if (doc.validFrom != null) {
			time.withReferenceYear(getYear(doc.validFrom));
			ext.setStartDate(doc.validFrom);
		}
		if (doc.validUntil != null) {
			time.withValidUntil(getYear(doc.validUntil));
			ext.setEndDate(doc.validUntil);
		}
		exp.add(time::withDescription, doc.time);
	}

	private Integer getYear(Date date) {
		if (date == null)
			return null;
		var cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	private void mapGeography(Process ds) {
		if (doc == null)
			return;
		if (process.location == null && doc.geography == null)
			return;
		var loc = ds.withProcessInfo()
				.withGeography()
				.withLocation();
		if (process.location != null) {
			var oLoc = process.location;
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
		if (doc == null)
			return;
		if (Strings.notEmpty(doc.technology)) {
			var tech = ds.withProcessInfo().withTechnology();
			exp.add(tech::withDescription, doc.technology);
		}
	}

	private void mapInventoryMethod(Process ds) {
		var method = ds.withModelling().withInventoryMethod();
		if (process.processType != null) {
			method.withProcessType(
					process.processType == ProcessType.UNIT_PROCESS
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
		if (process.defaultAllocationMethod == null)
			return null;
		return switch (process.defaultAllocationMethod) {
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
}
