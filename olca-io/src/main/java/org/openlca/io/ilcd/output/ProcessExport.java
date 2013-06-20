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
import org.openlca.core.model.AdminInfo;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelingAndValidation;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Technology;
import org.openlca.core.model.Time;
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
	private ModelingAndValidation modelingAndValidation;
	private IDatabase database;
	private DataStore dataStore;

	public ProcessExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public org.openlca.ilcd.processes.Process run(Process process)
			throws DataStoreException {
		log.trace("Run process export with {}", process);
		loadProcess(process);
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
		dataStore.put(iProcess, process.getId());
		return iProcess;
	}

	private void loadProcess(Process process) throws DataStoreException {
		try {
			this.process = database.createDao(Process.class).getForId(
					process.getId());
			this.modelingAndValidation = database.createDao(
					ModelingAndValidation.class).getForId(process.getId());
		} catch (Exception e) {
			throw new DataStoreException("Cannot load process from database.",
					e);
		}
	}

	private DataSetInformation makeDataSetInfo() {
		log.trace("Create data set info.");
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(process.getId());
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
		Time time = null;
		try {
			time = database.createDao(Time.class).getForId(process.getId());
		} catch (Exception e) {
			log.error("Cannot load process time id=" + process.getId(), e);
		}

		org.openlca.ilcd.commons.Time iTime = new org.openlca.ilcd.commons.Time();
		if (time != null) {
			mapTime(time, iTime);
		}
		return iTime;
	}

	private void mapTime(Time time, org.openlca.ilcd.commons.Time iTime) {
		log.trace("Map process time.");
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy");
		TimeExtension extension = new TimeExtension(iTime);
		if (time.getStartDate() != null) {
			String _start = dFormat.format(time.getStartDate());
			iTime.setReferenceYear(new BigInteger(_start));
			extension.setStartDate(time.getStartDate());
		}
		if (time.getEndDate() != null) {
			String _end = dFormat.format(time.getEndDate());
			iTime.setValidUntil(new BigInteger(_end));
			extension.setEndDate(time.getEndDate());
		}
		if (Strings.notEmpty(time.getComment())) {
			LangString.addFreeText(iTime.getDescription(), time.getComment());
		}
	}

	private Geography makeGeography() {
		log.trace("Create process geography.");
		if (process.getLocation() == null
				&& process.getGeographyComment() == null)
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
		if (Strings.notEmpty(process.getGeographyComment())) {
			LangString.addFreeText(iLocation.getDescription(),
					process.getGeographyComment());
		}
		return geography;
	}

	private org.openlca.ilcd.processes.Technology makeTechnology() {
		log.trace("Create process technology.");
		Technology technology = null;
		try {
			technology = database.createDao(Technology.class).getForId(
					process.getId());
		} catch (Exception e) {
			log.error("Cannot load process technology id=" + process.getId(), e);
		}

		if (technology == null)
			return null;

		org.openlca.ilcd.processes.Technology iTechnology = null;
		if (Strings.notEmpty(technology.getDescription())) {
			iTechnology = new org.openlca.ilcd.processes.Technology();
			LangString.addFreeText(
					iTechnology.getTechnologyDescriptionAndIncludedProcesses(),
					technology.getDescription());
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
			if (process.getProcessType() == ProcessType.UnitProcess) {
				iMethod.setProcessType(org.openlca.ilcd.commons.ProcessType.UNIT_PROCESS_BLACK_BOX);
			} else {
				iMethod.setProcessType(org.openlca.ilcd.commons.ProcessType.LCI_RESULT);
			}
		}

		iMethod.setLCIMethodPrinciple(LCIMethodPrinciple.OTHER);

		if (modelingAndValidation != null) {
			if (Strings.notEmpty(modelingAndValidation.getLCIMethod())) {
				iMethod.getDeviationsFromLCIMethodPrinciple().add(
						LangString.freeText(modelingAndValidation
								.getLCIMethod()));
			}

			if (Strings.notEmpty(modelingAndValidation.getModelingConstants())) {
				iMethod.getModellingConstants().add(
						LangString.freeText(modelingAndValidation
								.getModelingConstants()));
			}
		}

		LCIMethodApproach allocation = getAllocationMethod();
		if (allocation != null)
			iMethod.getLCIMethodApproaches().add(allocation);

		return iMethod;
	}

	private LCIMethodApproach getAllocationMethod() {
		if (process.getAllocationMethod() == null)
			return null;
		switch (process.getAllocationMethod()) {
		case Causal:
			return LCIMethodApproach.ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT;
		case Economic:
			return LCIMethodApproach.ALLOCATION_MARKET_VALUE;
		case Physical:
			return LCIMethodApproach.ALLOCATION_PHYSICAL_CAUSALITY;
		default:
			return null;
		}
	}

	private Representativeness makeRepresentativeness() {
		log.trace("Create process representativeness.");
		Representativeness iRepri = null;

		if (modelingAndValidation != null) {
			iRepri = new Representativeness();

			// completeness
			if (Strings.notEmpty(modelingAndValidation.getDataCompleteness())) {
				iRepri.getDataCutOffAndCompletenessPrinciples().add(
						LangString.freeText(modelingAndValidation
								.getDataCompleteness()));
				iRepri.getDeviationsFromCutOffAndCompletenessPrinciples().add(
						LangString.freeText("None."));
			}

			// data selection
			if (Strings.notEmpty(modelingAndValidation.getDataSelection())) {
				iRepri.getDataSelectionAndCombinationPrinciples().add(
						LangString.freeText(modelingAndValidation
								.getDataSelection()));
				iRepri.getDeviationsFromSelectionAndCombinationPrinciples()
						.add(LangString.freeText("None."));
			}

			// data treatment
			if (Strings.notEmpty(modelingAndValidation.getDataTreatment())) {
				iRepri.getDataTreatmentAndExtrapolationsPrinciples().add(
						LangString.freeText(modelingAndValidation
								.getDataTreatment()));
				iRepri.getDataTreatmentAndExtrapolationsPrinciples().add(
						LangString.freeText("None."));
			}

			// data sources
			for (Source source : modelingAndValidation.getSources()) {
				DataSetReference ref = ExportDispatch.forwardExportCheck(
						source, database, dataStore);
				if (ref != null)
					iRepri.getReferenceToDataSource().add(ref);
			}

			// sampling procedure
			if (Strings.notEmpty(modelingAndValidation.getSampling())) {
				iRepri.getSamplingProcedure()
						.add(LangString.freeText(modelingAndValidation
								.getSampling()));
			}

			// data collection period
			if (Strings.notEmpty(modelingAndValidation
					.getDataCollectionPeriod())) {
				iRepri.getDataCollectionPeriod().add(
						LangString.label(modelingAndValidation
								.getDataCollectionPeriod()));
			}
		}

		return iRepri;
	}

	private List<Review> makeReviews() {
		log.trace("Create process reviews.");
		List<Review> reviews = new ArrayList<>();
		if (modelingAndValidation != null
				&& modelingAndValidation.getReviewer() != null
				|| modelingAndValidation.getDataSetOtherEvaluation() != null) {

			Review review = new Review();
			reviews.add(review);
			review.setType(ReviewType.NOT_REVIEWED);

			if (modelingAndValidation.getReviewer() != null) {
				DataSetReference ref = ExportDispatch.forwardExportCheck(
						modelingAndValidation.getReviewer(), database,
						dataStore);
				if (ref != null)
					review.getReferenceToNameOfReviewerAndInstitution()
							.add(ref);
			}

			if (Strings.notEmpty(modelingAndValidation
					.getDataSetOtherEvaluation())) {
				review.getReviewDetails().add(
						LangString.freeText(modelingAndValidation
								.getDataSetOtherEvaluation()));
			}
		}
		return reviews;
	}

	private AdministrativeInformation makeAdminInformation() {
		log.trace("Create process administrative information.");
		AdminInfo adminInfo = null;
		try {
			adminInfo = database.createDao(AdminInfo.class).getForId(
					process.getId());
		} catch (Exception e) {
			log.error("Cannot load administrative information id="
					+ process.getId());
		}
		if (adminInfo == null)
			return null;
		ProcessAdminInfo processAdminInfo = new ProcessAdminInfo(adminInfo);
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
