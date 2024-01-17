package org.openlca.io.ilcd.output;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessDoc;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.Completeness;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.ImpactCategory;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ReviewMethod;
import org.openlca.ilcd.commons.ReviewScope;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.processes.FlowCompletenessEntry;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.util.Processes;
import org.openlca.ilcd.util.TimeExtension;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

		ds.adminInfo = new ProcessAdminInfo(exp).create(p);
		var params = new ProcessParameterConversion(exp).run(p);
		if (!params.isEmpty()) {
			Processes.forceParameters(ds).addAll(params);
		}
		Exchange qRef = p.quantitativeReference;
		if (qRef != null) {
			Processes.forceReferenceFlows(ds).add(qRef.internalId);
		}

		new ExchangeConversion(p, exp).run(ds);
		exp.store.put(ds);
	}

	private void mapDataSetInfo(Process ds) {
		var info = Processes.forceDataSetInfo(ds);
		info.uuid = process.refId;
		var processName = new ProcessName();
		info.name = processName;
		exp.add(processName.name, process.name);
		exp.add(info.comment, process.description);
		Categories.toClassification(process.category)
				.ifPresent(info.classifications::add);
	}

	private void mapTime(Process ds) {
		if (doc == null)
			return;
		var time = Processes.forceTime(ds);
		var ext = new TimeExtension(time);
		if (doc.validFrom != null) {
			time.referenceYear = getYear(doc.validFrom);
			ext.setStartDate(doc.validFrom);
		}
		if (doc.validUntil != null) {
			time.validUntil = getYear(doc.validUntil);
			ext.setEndDate(doc.validUntil);
		}
		exp.add(time.description, doc.time);
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
		var loc = Processes.forceLocation(ds);
		if (process.location != null) {
			var oLoc = process.location;
			loc.code = oLoc.code;
			// do not write (0.0, 0.0) locations; these are the default
			// location coordinates in openLCA but probably never a valid
			// process location, right?
			if (!(oLoc.latitude == 0.0 && oLoc.longitude == 0.0)) {
				loc.latitudeAndLongitude = oLoc.latitude + ";" + oLoc.longitude;
			}
		}
		exp.add(loc.description, doc.geography);
	}

	private void mapTechnology(Process ds) {
		if (doc == null)
			return;
		if (Strings.notEmpty(doc.technology)) {
			var tech = Processes.forceTechnology(ds);
			exp.add(tech.description, doc.technology);
		}
	}

	private void mapInventoryMethod(Process ds) {
		var method = Processes.forceInventoryMethod(ds);
		if (process.processType != null) {
			method.processType = process.processType == ProcessType.UNIT_PROCESS
					? org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX
					: org.openlca.ilcd.commons.ProcessType.LCI_RESULT;
		}
		method.principle = ModellingPrinciple.OTHER;
		if (doc != null) {
			exp.add(method.principleDeviations, doc.inventoryMethod);
			exp.add(method.constants, doc.modelingConstants);
		}
		var allocation = getAllocationMethod();
		if (allocation != null) {
			method.approaches.add(allocation);
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
		var rep = Processes.forceRepresentativeness(ds);
		exp.add(rep.completeness, doc.dataCompleteness);
		exp.add(rep.completenessComment, "None.");
		exp.add(rep.dataSelection, doc.dataSelection);
		exp.add(rep.dataSelectionComment, "None.");
		exp.add(rep.dataTreatment, doc.dataTreatment);
		exp.add(rep.samplingProcedure, doc.samplingProcedure);
		exp.add(rep.dataCollectionPeriod, doc.dataCollectionPeriod);
		for (Source source : doc.sources) {
			Ref ref = exp.writeRef(source);
			if (ref != null) {
				rep.sources.add(ref);
			}
		}
	}

	private void mapReviews(Process ds) {
		if (doc == null || doc.reviews.isEmpty())
			return;
		var reviews = Processes.forceReviews(ds);
		for (var r : doc.reviews) {
			var rev = new Review();
			reviews.add(rev);
			rev.type = ReviewType.fromValue(r.type).orElse(null);
			rev.report = exp.writeRef(r.report);
			exp.add(rev.details, r.details);
			for (var reviewer : r.reviewers) {
				var ref = exp.writeRef(reviewer);
				if (ref != null) {
					rev.reviewers.add(ref);
				}
			}

			for (var s : r.scopes) {
				var scope = new Review.Scope();
				scope.name = ReviewScope.fromValue(s.name).orElse(null);
				if (scope.name == null)
					continue;
				rev.scopes.add(scope);
				for (var m : s.methods) {
					ReviewMethod.fromValue(m).ifPresent(method -> {
						var entry = new Review.Method();
						entry.name = method;
						scope.methods.add(entry);
					});
				}
			}
		}
	}

	private void mapCompleteness(Process ds) {
		var c = Completeness.readFrom(process);
		if (c.isEmpty())
			return;
		var target = Processes.forceCompleteness(ds);
		target.productCompleteness = FlowCompleteness
				.fromValue(c.get("Product model"))
				.orElse(null);
		for (var impact : ImpactCategory.values()) {
			var value = FlowCompleteness.fromValue(c.get(impact.value()))
					.orElse(null);
			if (value == null)
				continue;
			var e = new FlowCompletenessEntry();
			e.impact = impact;
			e.value = value;
			target.entries.add(e);
		}
	}
}
