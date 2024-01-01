package org.openlca.io.ilcd.output;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.util.ProcessBuilder;
import org.openlca.ilcd.util.TimeExtension;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * The export of an openLCA process to an ILCD process data set.
 */
public class ProcessExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Export exp;
	private org.openlca.core.model.Process process;
	private ProcessDocumentation doc;

	public ProcessExport(Export exp) {
		this.exp = exp;
	}

	public Process run(org.openlca.core.model.Process p) {
		if (exp.store.contains(Process.class, p.refId))
			return exp.store.get(Process.class, p.refId);
		log.trace("Run process export with {}", p);
		this.process = p;
		this.doc = p.documentation;

		ProcessBuilder builder = ProcessBuilder.makeProcess()
				.with(makeLciMethod())
				.withAdminInfo(new ProcessAdminInfo(exp).create(p))
				.withDataSetInfo(makeDataSetInfo())
				.withGeography(makeGeography())
				.withParameters(new ProcessParameterConversion(exp).run(p))
				.withRepresentativeness(makeRepresentativeness())
				.withReviews(makeReviews())
				.withTechnology(makeTechnology())
				.withTime(makeTime());

		Exchange qRef = p.quantitativeReference;
		if (qRef != null) {
			builder.withReferenceFlowId(qRef.internalId);
		}
		Process iProcess = builder.getProcess();
		new ExchangeConversion(p, exp).run(iProcess);
		exp.store.put(iProcess);
		return iProcess;
	}

	private DataSetInfo makeDataSetInfo() {
		log.trace("Create data set info.");
		var info = new DataSetInfo();
		info.uuid = process.refId;
		var processName = new ProcessName();
		info.name = processName;
		exp.add(processName.name, process.name);
		exp.add(info.comment, process.description);
		Categories.toClassification(process.category)
				.ifPresent(info.classifications::add);
		return info;
	}

	private org.openlca.ilcd.commons.Time makeTime() {
		log.trace("Create process time.");
		Time iTime = new Time();
		if (doc == null)
			return iTime;
		var extension = new TimeExtension(iTime);
		if (doc.validFrom != null) {
			iTime.referenceYear = getYear(doc.validFrom);
			extension.setStartDate(doc.validFrom);
		}
		if (doc.validUntil != null) {
			iTime.validUntil = getYear(doc.validUntil);
			extension.setEndDate(doc.validUntil);
		}
		exp.add(iTime.description, doc.time);
		return iTime;
	}

	private Integer getYear(Date date) {
		if (date == null)
			return null;
		var cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	private Geography makeGeography() {
		log.trace("Create process geography.");
		if (doc == null)
			return null;
		if (process.location == null && doc.geography == null)
			return null;
		var geography = new Geography();
		var iLoc = new org.openlca.ilcd.processes.Location();
		geography.location = iLoc;
		if (process.location != null) {
			var oLoc = process.location;
			iLoc.code = oLoc.code;
			// do not write (0.0, 0.0) locations; these are the default
			// location coordinates in openLCA but probably never a valid
			// process location, right?
			if (!(oLoc.latitude == 0.0 && oLoc.longitude == 0.0)) {
				iLoc.latitudeAndLongitude = oLoc.latitude + ";" + oLoc.longitude;
			}
		}
		exp.add(iLoc.description, doc.geography);
		return geography;
	}

	private org.openlca.ilcd.processes.Technology makeTechnology() {
		log.trace("Create process technology.");
		if (doc == null)
			return null;

		org.openlca.ilcd.processes.Technology iTechnology = null;
		if (Strings.notEmpty(doc.technology)) {
			iTechnology = new org.openlca.ilcd.processes.Technology();
			exp.add(iTechnology.description, doc.technology);
		}
		return iTechnology;
	}

	private Method makeLciMethod() {
		log.trace("Create process LCI method.");
		Method iMethod = new Method();
		if (process.processType != null) {
			iMethod.processType = process.processType == ProcessType.UNIT_PROCESS
					? org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX
					: org.openlca.ilcd.commons.ProcessType.LCI_RESULT;
		}

		iMethod.principle = ModellingPrinciple.OTHER;

		if (doc != null) {
			exp.add(iMethod.principleComment, doc.inventoryMethod);
			exp.add(iMethod.constants, doc.modelingConstants);
		}

		var allocation = getAllocationMethod();
		if (allocation != null)
			iMethod.approaches.add(allocation);

		return iMethod;
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

	private Representativeness makeRepresentativeness() {
		log.trace("Create process representativeness.");
		if (doc == null)
			return null;
		var iRepri = new Representativeness();

		exp.add(iRepri.completeness, doc.completeness);
		exp.add(iRepri.completenessComment, "None.");
		exp.add(iRepri.dataSelection, doc.dataSelection);
		exp.add(iRepri.dataSelectionComment, "None.");
		exp.add(iRepri.dataTreatment, doc.dataTreatment);

		for (Source source : doc.sources) {
			Ref ref = exp.writeRef(source);
			if (ref != null)
				iRepri.sources.add(ref);
		}

		exp.add(iRepri.samplingProcedure, doc.sampling);
		exp.add(iRepri.dataCollectionPeriod, doc.dataCollectionPeriod);

		return iRepri;
	}

	private List<Review> makeReviews() {
		log.trace("Create process reviews.");
		List<Review> reviews = new ArrayList<>();
		if (doc == null)
			return reviews;
		if (doc.reviewer == null && doc.reviewDetails == null)
			return reviews;
		Review review = new Review();
		reviews.add(review);
		review.type = ReviewType.NOT_REVIEWED;
		if (doc.reviewer != null) {
			Ref ref = exp.writeRef(doc.reviewer);
			if (ref != null)
				review.reviewers.add(ref);
		}
		exp.add(review.details, doc.reviewDetails);
		return reviews;
	}
}
