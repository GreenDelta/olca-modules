package org.openlca.io.ilcd.output;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.LangString;
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
	private final ExportConfig config;
	private org.openlca.core.model.Process process;
	private ProcessDocumentation doc;

	public ProcessExport(ExportConfig config) {
		this.config = config;
	}

	public Process run(org.openlca.core.model.Process p) {
		if (config.store.contains(Process.class, p.refId))
			return config.store.get(Process.class, p.refId);
		log.trace("Run process export with {}", p);
		this.process = p;
		this.doc = p.documentation;

		ProcessBuilder builder = ProcessBuilder.makeProcess()
				.with(makeLciMethod())
				.withAdminInfo(new ProcessAdminInfo(config).create(p))
				.withDataSetInfo(makeDataSetInfo())
				.withGeography(makeGeography())
				.withParameters(new ProcessParameterConversion(config).run(p))
				.withRepresentativeness(makeRepresentativeness())
				.withReviews(makeReviews())
				.withTechnology(makeTechnology())
				.withTime(makeTime());

		Exchange qRef = p.quantitativeReference;
		if (qRef != null) {
			builder.withReferenceFlowId(qRef.internalId);
		}
		Process iProcess = builder.getProcess();
		new ExchangeConversion(p, config).run(iProcess);
		config.store.put(iProcess);
		return iProcess;
	}

	private DataSetInfo makeDataSetInfo() {
		log.trace("Create data set info.");
		DataSetInfo dataSetInfo = new DataSetInfo();
		dataSetInfo.uuid = process.refId;
		ProcessName processName = new ProcessName();
		dataSetInfo.name = processName;
		s(processName.name, process.name);
		s(dataSetInfo.comment, process.description);
		addClassification(dataSetInfo);
		return dataSetInfo;
	}

	private void addClassification(DataSetInfo dataSetInfo) {
		log.trace("Add classification");
		if (process.category != null) {
			CategoryConverter converter = new CategoryConverter();
			Classification c = converter.getClassification(
					process.category);
			if (c != null)
				dataSetInfo.classifications.add(c);
		}
	}

	private org.openlca.ilcd.commons.Time makeTime() {
		log.trace("Create process time.");
		Time iTime = new Time();
		if (doc == null)
			return iTime;
		TimeExtension extension = new TimeExtension(iTime);
		if (doc.validFrom != null) {
			iTime.referenceYear = getYear(doc.validFrom);
			extension.setStartDate(doc.validFrom);
		}
		if (doc.validUntil != null) {
			iTime.validUntil = getYear(doc.validUntil);
			extension.setEndDate(doc.validUntil);
		}
		s(iTime.description, doc.time);
		return iTime;
	}

	private Integer getYear(Date date) {
		if (date == null)
			return null;
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	private Geography makeGeography() {
		log.trace("Create process geography.");
		if (doc == null)
			return null;
		if (process.location == null && doc.geography == null)
			return null;
		Geography geography = new Geography();
		org.openlca.ilcd.processes.Location iLoc = new org.openlca.ilcd.processes.Location();
		geography.location = iLoc;
		if (process.location != null) {
			Location oLoc = process.location;
			iLoc.code = oLoc.code;
			// do not write (0.0, 0.0) locations; these are the default
			// location coordinates in openLCA but probably never a valid
			// process location, right?
			if (!(oLoc.latitude == 0.0 && oLoc.longitude == 0.0)) {
				iLoc.latitudeAndLongitude = oLoc.latitude + ";" + oLoc.longitude;
			}
		}
		s(iLoc.description, doc.geography);
		return geography;
	}

	private org.openlca.ilcd.processes.Technology makeTechnology() {
		log.trace("Create process technology.");
		if (doc == null)
			return null;

		org.openlca.ilcd.processes.Technology iTechnology = null;
		if (Strings.notEmpty(doc.technology)) {
			iTechnology = new org.openlca.ilcd.processes.Technology();
			s(iTechnology.description,
					doc.technology);
		}
		return iTechnology;
	}

	private Method makeLciMethod() {
		log.trace("Create process LCI method.");
		Method iMethod = new Method();
		if (process.processType != null) {
			if (process.processType == ProcessType.UNIT_PROCESS) {
				iMethod.processType = org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX;
			} else {
				iMethod.processType = org.openlca.ilcd.commons.ProcessType.LCI_RESULT;
			}
		}

		iMethod.principle = ModellingPrinciple.OTHER;

		if (doc != null) {
			s(iMethod.principleComment,
					doc.inventoryMethod);
			s(iMethod.constants,
					doc.modelingConstants);
		}

		ModellingApproach allocation = getAllocationMethod();
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
		Representativeness iRepri = new Representativeness();

		s(iRepri.completeness, doc.completeness);
		s(iRepri.completenessComment, "None.");
		s(iRepri.dataSelection, doc.dataSelection);
		s(iRepri.dataSelectionComment, "None.");
		s(iRepri.dataTreatment, doc.dataTreatment);

		for (Source source : doc.sources) {
			Ref ref = Export.of(source, config);
			if (ref != null)
				iRepri.sources.add(ref);
		}

		s(iRepri.samplingProcedure, doc.sampling);
		s(iRepri.dataCollectionPeriod, doc.dataCollectionPeriod);

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
			Ref ref = Export.of(doc.reviewer, config);
			if (ref != null)
				review.reviewers.add(ref);
		}
		s(review.details, doc.reviewDetails);
		return reviews;
	}

	private void s(List<LangString> list, String val) {
		if (Strings.nullOrEmpty(val))
			return;
		LangString.set(list, val, config.lang);
	}

}
