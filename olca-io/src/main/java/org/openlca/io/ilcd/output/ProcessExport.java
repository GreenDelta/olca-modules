package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Location;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.LCIMethodApproach;
import org.openlca.ilcd.commons.LCIMethodPrinciple;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.processes.AdministrativeInformation;
import org.openlca.ilcd.processes.DataSetInformation;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.LCIMethod;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.util.LangString;
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
		config.store.put(iProcess, process.getRefId());
		return iProcess;
	}

	private DataSetInformation makeDataSetInfo() {
		log.trace("Create data set info.");
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(process.getRefId());
		ProcessName processName = new ProcessName();
		dataSetInfo.setName(processName);
		addLabel(processName.getBaseName(), process.getName());
		if (Strings.notEmpty(process.getDescription())) {
			addText(dataSetInfo.getGeneralComment(), process.getDescription());
		}
		addClassification(dataSetInfo);
		return dataSetInfo;
	}

	private void addClassification(DataSetInformation dataSetInfo) {
		log.trace("Add classification");
		if (process.getCategory() != null) {
			CategoryConverter converter = new CategoryConverter();
			ClassificationInformation classification = converter
					.getClassificationInformation(process.getCategory());
			if (classification != null) {
				dataSetInfo.setClassificationInformation(classification);
			}
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
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy");
		TimeExtension extension = new TimeExtension(iTime);
		if (doc.getValidFrom() != null) {
			String _start = dFormat.format(doc.getValidFrom());
			iTime.setReferenceYear(new BigInteger(_start));
			extension.setStartDate(doc.getValidFrom());
		}
		if (doc.getValidUntil() != null) {
			String _end = dFormat.format(doc.getValidUntil());
			iTime.setValidUntil(new BigInteger(_end));
			extension.setEndDate(doc.getValidUntil());
		}
		if (Strings.notEmpty(doc.getTime())) {
			addText(iTime.getDescription(), doc.getTime());
		}
	}

	private Geography makeGeography() {
		log.trace("Create process geography.");
		if (doc == null)
			return null;
		if (process.getLocation() == null && doc.getGeography() == null)
			return null;
		Geography geography = new Geography();
		org.openlca.ilcd.processes.Location iLocation = new org.openlca.ilcd.processes.Location();
		geography.setLocation(iLocation);
		if (process.getLocation() != null) {
			Location oLocation = process.getLocation();
			iLocation.setLocation(oLocation.getCode());
			String pos = "" + oLocation.getLatitude() + ","
					+ oLocation.getLongitude();
			iLocation.setLatitudeAndLongitude(pos);
		}
		if (Strings.notEmpty(doc.getGeography())) {
			addText(iLocation.getDescription(), doc.getGeography());
		}
		return geography;
	}

	private org.openlca.ilcd.processes.Technology makeTechnology() {
		log.trace("Create process technology.");
		if (doc == null)
			return null;

		org.openlca.ilcd.processes.Technology iTechnology = null;
		if (Strings.notEmpty(doc.getTechnology())) {
			iTechnology = new org.openlca.ilcd.processes.Technology();
			addText(iTechnology.getTechnologyDescriptionAndIncludedProcesses(),
					doc.getTechnology());
		}
		return iTechnology;
	}

	private List<Parameter> makeParameters() {
		log.trace("Create process parameters.");
		ProcessParameterConversion conv = new ProcessParameterConversion(config);
		return conv.run(process);
	}

	private LCIMethod makeLciMethod() {
		log.trace("Create process LCI method.");
		LCIMethod iMethod = new LCIMethod();
		if (process.getProcessType() != null) {
			if (process.getProcessType() == ProcessType.UNIT_PROCESS) {
				iMethod.setProcessType(org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX);
			} else {
				iMethod.setProcessType(org.openlca.ilcd.commons.ProcessType.LCI_RESULT);
			}
		}

		iMethod.setLCIMethodPrinciple(LCIMethodPrinciple.OTHER);

		if (doc != null) {
			if (Strings.notEmpty(doc.getInventoryMethod()))
				addText(iMethod.getDeviationsFromLCIMethodPrinciple(),
						doc.getInventoryMethod());
			if (Strings.notEmpty(doc.getModelingConstants()))
				addText(iMethod.getModellingConstants(),
						doc.getModelingConstants());
		}

		LCIMethodApproach allocation = getAllocationMethod();
		if (allocation != null)
			iMethod.getLCIMethodApproaches().add(allocation);

		return iMethod;
	}

	private LCIMethodApproach getAllocationMethod() {
		if (process.getDefaultAllocationMethod() == null)
			return null;
		switch (process.getDefaultAllocationMethod()) {
		case CAUSAL:
			return LCIMethodApproach.ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT;
		case ECONOMIC:
			return LCIMethodApproach.ALLOCATION_MARKET_VALUE;
		case PHYSICAL:
			return LCIMethodApproach.ALLOCATION_PHYSICAL_CAUSALITY;
		default:
			return null;
		}
	}

	private Representativeness makeRepresentativeness() {
		log.trace("Create process representativeness.");
		Representativeness iRepri = null;

		if (doc != null) {
			iRepri = new Representativeness();

			// completeness
			if (Strings.notEmpty(doc.getCompleteness())) {
				addText(iRepri.getDataCutOffAndCompletenessPrinciples(),
						doc.getCompleteness());
				addText(iRepri
						.getDeviationsFromCutOffAndCompletenessPrinciples(),
						"None.");
			}

			// data selection
			if (Strings.notEmpty(doc.getDataSelection())) {
				addText(iRepri.getDataSelectionAndCombinationPrinciples(),
						doc.getDataSelection());
				addText(iRepri
						.getDeviationsFromSelectionAndCombinationPrinciples(),
						"None.");
			}

			// data treatment
			if (Strings.notEmpty(doc.getDataTreatment())) {
				List<FreeText> ePrinciples = iRepri
						.getDataTreatmentAndExtrapolationsPrinciples();
				addText(ePrinciples, doc.getDataTreatment());
				addText(ePrinciples, "None.");
			}

			// data sources
			for (Source source : doc.getSources()) {
				DataSetReference ref = ExportDispatch.forwardExportCheck(
						source, config);
				if (ref != null)
					iRepri.getReferenceToDataSource().add(ref);
			}

			// sampling procedure
			if (Strings.notEmpty(doc.getSampling()))
				addText(iRepri.getSamplingProcedure(), doc.getSampling());

			// data collection period
			if (Strings.notEmpty(doc.getDataCollectionPeriod()))
				addLabel(iRepri.getDataCollectionPeriod(),
						doc.getDataCollectionPeriod());
		}

		return iRepri;
	}

	private List<Review> makeReviews() {
		log.trace("Create process reviews.");
		List<Review> reviews = new ArrayList<>();
		if (doc != null && doc.getReviewer() != null
				|| doc.getReviewDetails() != null) {

			Review review = new Review();
			reviews.add(review);
			review.setType(ReviewType.NOT_REVIEWED);

			if (doc.getReviewer() != null) {
				DataSetReference ref = ExportDispatch.forwardExportCheck(
						doc.getReviewer(), config);
				if (ref != null)
					review.getReferenceToNameOfReviewerAndInstitution()
							.add(ref);
			}

			if (Strings.notEmpty(doc.getReviewDetails())) {
				addText(review.getReviewDetails(), doc.getReviewDetails());
			}
		}
		return reviews;
	}

	private AdministrativeInformation makeAdminInformation() {
		log.trace("Create process administrative information.");
		if (doc == null)
			return null;
		ProcessAdminInfo processAdminInfo = new ProcessAdminInfo(config);
		AdministrativeInformation iAdminInfo = processAdminInfo.create(process);
		return iAdminInfo;
	}

	private void addExchanges(org.openlca.ilcd.processes.Process ilcdProcess) {
		log.trace("Create process exchanges.");
		ExchangeConversion conversion = new ExchangeConversion(process, config);
		conversion.run(ilcdProcess);
	}

	private void addLabel(List<Label> labels, String value) {
		if (Strings.nullOrEmpty(value))
			return;
		LangString.addLabel(labels, value, config.ilcdConfig);
	}

	private void addText(List<FreeText> texts, String value) {
		if (Strings.nullOrEmpty(value))
			return;
		LangString.addFreeText(texts, value, config.ilcdConfig);
	}

}
