package org.openlca.io.ilcd.output;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.util.ProcessBuilder;
import org.openlca.ilcd.util.TimeExtension;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public Process run(org.openlca.core.model.Process process)
			throws DataStoreException {
		if (config.store.contains(Process.class, process.getRefId()))
			return config.store.get(Process.class, process.getRefId());
		log.trace("Run process export with {}", process);
		this.process = process;
		this.doc = process.getDocumentation();
		Process iProcess = ProcessBuilder.makeProcess().with(makeLciMethod())
				.withAdminInfo(makeAdminInformation())
				.withDataSetInfo(makeDataSetInfo())
				.withGeography(makeGeography())
				.withParameters(makeParameters()).withReferenceFlowId(0)
				.withRepresentativeness(makeRepresentativeness())
				.withReviews(makeReviews()).withTechnology(makeTechnology())
				.withTime(makeTime()).getProcess();
		addExchanges(iProcess);
		config.store.put(iProcess);
		return iProcess;
	}

	private DataSetInfo makeDataSetInfo() {
		log.trace("Create data set info.");
		DataSetInfo dataSetInfo = new DataSetInfo();
		dataSetInfo.uuid = process.getRefId();
		ProcessName processName = new ProcessName();
		dataSetInfo.name = processName;
		s(processName.name, process.getName());
		s(dataSetInfo.comment, process.getDescription());
		addClassification(dataSetInfo);
		return dataSetInfo;
	}

	private void addClassification(DataSetInfo dataSetInfo) {
		log.trace("Add classification");
		if (process.getCategory() != null) {
			CategoryConverter converter = new CategoryConverter();
			Classification c = converter.getClassification(
					process.getCategory());
			if (c != null)
				dataSetInfo.classifications.add(c);
		}
	}

	private org.openlca.ilcd.commons.Time makeTime() {
		log.trace("Create process time.");
		org.openlca.ilcd.commons.Time iTime = new org.openlca.ilcd.commons.Time();
		mapTime(iTime);
		return iTime;
	}

	private void mapTime(org.openlca.ilcd.commons.Time iTime) {
		log.trace("Map process time.");
		if (doc == null)
			return;
		TimeExtension extension = new TimeExtension(iTime);
		if (doc.getValidFrom() != null) {
			iTime.referenceYear = getYear(doc.getValidFrom());
			extension.setStartDate(doc.getValidFrom());
		}
		if (doc.getValidUntil() != null) {
			iTime.validUntil = getYear(doc.getValidUntil());
			extension.setEndDate(doc.getValidUntil());
		}
		s(iTime.description, doc.getTime());
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
		if (process.getLocation() == null && doc.getGeography() == null)
			return null;
		Geography geography = new Geography();
		org.openlca.ilcd.processes.Location iLocation = new org.openlca.ilcd.processes.Location();
		geography.location = iLocation;
		if (process.getLocation() != null) {
			Location oLocation = process.getLocation();
			iLocation.code = oLocation.getCode();
			String pos = "" + oLocation.getLatitude() + ","
					+ oLocation.getLongitude();
			iLocation.latitudeAndLongitude = pos;
		}
		s(iLocation.description, doc.getGeography());
		return geography;
	}

	private org.openlca.ilcd.processes.Technology makeTechnology() {
		log.trace("Create process technology.");
		if (doc == null)
			return null;

		org.openlca.ilcd.processes.Technology iTechnology = null;
		if (Strings.notEmpty(doc.getTechnology())) {
			iTechnology = new org.openlca.ilcd.processes.Technology();
			s(iTechnology.description,
					doc.getTechnology());
		}
		return iTechnology;
	}

	private List<Parameter> makeParameters() {
		log.trace("Create process parameters.");
		ProcessParameterConversion conv = new ProcessParameterConversion(config);
		return conv.run(process);
	}

	private Method makeLciMethod() {
		log.trace("Create process LCI method.");
		Method iMethod = new Method();
		if (process.getProcessType() != null) {
			if (process.getProcessType() == ProcessType.UNIT_PROCESS) {
				iMethod.processType = org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX;
			} else {
				iMethod.processType = org.openlca.ilcd.commons.ProcessType.LCI_RESULT;
			}
		}

		iMethod.principle = ModellingPrinciple.OTHER;

		if (doc != null) {
			s(iMethod.principleComment,
					doc.getInventoryMethod());
			s(iMethod.constants,
					doc.getModelingConstants());
		}

		ModellingApproach allocation = getAllocationMethod();
		if (allocation != null)
			iMethod.approaches.add(allocation);

		return iMethod;
	}

	private ModellingApproach getAllocationMethod() {
		if (process.getDefaultAllocationMethod() == null)
			return null;
		switch (process.getDefaultAllocationMethod()) {
		case CAUSAL:
			return ModellingApproach.ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT;
		case ECONOMIC:
			return ModellingApproach.ALLOCATION_MARKET_VALUE;
		case PHYSICAL:
			return ModellingApproach.ALLOCATION_PHYSICAL_CAUSALITY;
		default:
			return null;
		}
	}

	private Representativeness makeRepresentativeness() {
		log.trace("Create process representativeness.");
		if (doc == null)
			return null;
		Representativeness iRepri = new Representativeness();

		// completeness
		s(iRepri.completeness, doc.getCompleteness());
		s(iRepri.completenessComment, "None.");

		// data selection
		s(iRepri.dataSelection, doc.getDataSelection());
		s(iRepri.dataSelectionComment, "None.");

		// data treatment
		s(iRepri.dataTreatment, doc.getDataTreatment());

		// data sources
		for (Source source : doc.getSources()) {
			Ref ref = ExportDispatch.forwardExportCheck(
					source, config);
			if (ref != null)
				iRepri.sources.add(ref);
		}

		// sampling procedure
		s(iRepri.samplingProcedure, doc.getSampling());

		// data collection period
		s(iRepri.dataCollectionPeriod, doc.getDataCollectionPeriod());

		return iRepri;
	}

	private List<Review> makeReviews() {
		log.trace("Create process reviews.");
		List<Review> reviews = new ArrayList<>();
		if (doc != null && doc.getReviewer() != null
				|| doc.getReviewDetails() != null) {

			Review review = new Review();
			reviews.add(review);
			review.type = ReviewType.NOT_REVIEWED;

			if (doc.getReviewer() != null) {
				Ref ref = ExportDispatch.forwardExportCheck(
						doc.getReviewer(), config);
				if (ref != null)
					review.reviewers.add(ref);
			}

			s(review.details, doc.getReviewDetails());
		}
		return reviews;
	}

	private AdminInfo makeAdminInformation() {
		log.trace("Create process administrative information.");
		if (doc == null)
			return null;
		ProcessAdminInfo processAdminInfo = new ProcessAdminInfo(config);
		AdminInfo iAdminInfo = processAdminInfo.create(process);
		return iAdminInfo;
	}

	private void addExchanges(org.openlca.ilcd.processes.Process ilcdProcess) {
		log.trace("Create process exchanges.");
		ExchangeConversion conversion = new ExchangeConversion(process, config);
		conversion.run(ilcdProcess);
	}

	private void s(List<LangString> list, String val) {
		if (Strings.nullOrEmpty(val))
			return;
		LangString.set(list, val, config.lang);

	}

}
