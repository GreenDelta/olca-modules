/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/

package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LCIMethodApproach;
import org.openlca.ilcd.commons.LCIMethodPrinciple;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.processes.AdministrativeInformation;
import org.openlca.ilcd.processes.DataSetInformation;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.LCIMethod;
import org.openlca.ilcd.processes.Parameter;
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

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Process process;
	private ProcessDocumentation doc;
	private IDatabase database;
	private DataStore dataStore;

	public ProcessExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public org.openlca.ilcd.processes.Process run(Process process)
			throws DataStoreException {
		log.trace("Run process export with {}", process);
		this.process = process;
		this.doc = process.getDocumentation();
		org.openlca.ilcd.processes.Process iProcess = ProcessBuilder
				.makeProcess().with(makeLciMethod())
				.withAdminInfo(makeAdminInformation())
				.withDataSetInfo(makeDataSetInfo())
				.withGeography(makeGeography())
				.withParameters(makeParameters()).withReferenceFlowId(0)
				.withRepresentativeness(makeRepresentativeness())
				.withReviews(makeReviews()).withTechnology(makeTechnology())
				.withTime(makeTime()).getProcess();
		addExchanges(iProcess);
		dataStore.put(iProcess, process.getRefId());
		return iProcess;
	}

	private DataSetInformation makeDataSetInfo() {
		log.trace("Create data set info.");
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(process.getRefId());
		ProcessName processName = new ProcessName();
		dataSetInfo.setName(processName);
		LangString.addLabel(processName.getBaseName(), process.getName());
		if (Strings.notEmpty(process.getDescription())) {
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					process.getDescription());
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
			LangString.addFreeText(iTime.getDescription(), doc.getTime());
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
			LangString.addFreeText(iLocation.getDescription(),
					doc.getGeography());
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
			LangString.addFreeText(
					iTechnology.getTechnologyDescriptionAndIncludedProcesses(),
					doc.getTechnology());
		}
		return iTechnology;
	}

	private List<Parameter> makeParameters() {
		log.trace("Create process parameters.");
		ProcessParameterConversion conv = new ProcessParameterConversion(
				process, database);
		return conv.run();
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
			if (Strings.notEmpty(doc.getInventoryMethod())) {
				iMethod.getDeviationsFromLCIMethodPrinciple().add(
						LangString.freeText(doc.getInventoryMethod()));
			}

			if (Strings.notEmpty(doc.getModelingConstants())) {
				iMethod.getModellingConstants().add(
						LangString.freeText(doc.getModelingConstants()));
			}
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
				iRepri.getDataCutOffAndCompletenessPrinciples().add(
						LangString.freeText(doc.getCompleteness()));
				iRepri.getDeviationsFromCutOffAndCompletenessPrinciples().add(
						LangString.freeText("None."));
			}

			// data selection
			if (Strings.notEmpty(doc.getDataSelection())) {
				iRepri.getDataSelectionAndCombinationPrinciples().add(
						LangString.freeText(doc.getDataSelection()));
				iRepri.getDeviationsFromSelectionAndCombinationPrinciples()
						.add(LangString.freeText("None."));
			}

			// data treatment
			if (Strings.notEmpty(doc.getDataTreatment())) {
				iRepri.getDataTreatmentAndExtrapolationsPrinciples().add(
						LangString.freeText(doc.getDataTreatment()));
				iRepri.getDataTreatmentAndExtrapolationsPrinciples().add(
						LangString.freeText("None."));
			}

			// data sources
			for (Source source : doc.getSources()) {
				DataSetReference ref = ExportDispatch.forwardExportCheck(
						source, database, dataStore);
				if (ref != null)
					iRepri.getReferenceToDataSource().add(ref);
			}

			// sampling procedure
			if (Strings.notEmpty(doc.getSampling())) {
				iRepri.getSamplingProcedure().add(
						LangString.freeText(doc.getSampling()));
			}

			// data collection period
			if (Strings.notEmpty(doc.getDataCollectionPeriod())) {
				iRepri.getDataCollectionPeriod().add(
						LangString.label(doc.getDataCollectionPeriod()));
			}
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
						doc.getReviewer(), database, dataStore);
				if (ref != null)
					review.getReferenceToNameOfReviewerAndInstitution()
							.add(ref);
			}

			if (Strings.notEmpty(doc.getReviewDetails())) {
				review.getReviewDetails().add(
						LangString.freeText(doc.getReviewDetails()));
			}
		}
		return reviews;
	}

	private AdministrativeInformation makeAdminInformation() {
		log.trace("Create process administrative information.");
		if (doc == null)
			return null;
		ProcessAdminInfo processAdminInfo = new ProcessAdminInfo(doc);
		AdministrativeInformation iAdminInfo = processAdminInfo.create(
				database, dataStore);
		return iAdminInfo;
	}

	private void addExchanges(org.openlca.ilcd.processes.Process ilcdProcess) {
		log.trace("Create process exchanges.");
		ExchangeConversion conversion = new ExchangeConversion(process,
				database, dataStore);
		conversion.run(ilcdProcess);
	}

}
