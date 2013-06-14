package org.openlca.io.ilcd.input;

import java.util.Date;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AdminInfo;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ModelingAndValidation;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Technology;
import org.openlca.core.model.Time;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LCIMethodApproach;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.LCIMethod;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.io.KeyGen;
import org.openlca.io.maps.FlowMap;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private DataStore dataStore;
	private ProcessBag ilcdProcess;
	private Process process;
	private FlowMap flowMap;

	public ProcessImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public void setFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
	}

	public Process run(org.openlca.ilcd.processes.Process process)
			throws ImportException {
		this.ilcdProcess = new ProcessBag(process);
		Process oProcess = findExisting(ilcdProcess.getId());
		if (oProcess != null)
			return oProcess;
		return createNew();
	}

	public Process run(String processId) throws ImportException {
		Process process = findExisting(processId);
		if (process != null)
			return process;
		org.openlca.ilcd.processes.Process iProcess = tryGetProcess(processId);
		ilcdProcess = new ProcessBag(iProcess);
		return createNew();
	}

	private Process findExisting(String processId) throws ImportException {
		try {
			return database.createDao(Process.class).getForId(processId);
		} catch (Exception e) {
			String message = String.format("Search for process %s failed.",
					processId);
			throw new ImportException(message, e);
		}
	}

	private Process createNew() throws ImportException {
		process = new Process();
		importAndSetCategory();
		createAndMapContent();
		saveInDatabase(process);
		return process;
	}

	private org.openlca.ilcd.processes.Process tryGetProcess(String processId)
			throws ImportException {
		try {
			org.openlca.ilcd.processes.Process iProcess = dataStore.get(
					org.openlca.ilcd.processes.Process.class, processId);
			if (iProcess == null) {
				throw new ImportException("No ILCD process for ID " + processId
						+ " found");
			}
			return iProcess;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(database,
				ModelType.PROCESS);
		Category category = categoryImport.run(ilcdProcess.getSortedClasses());
		process.setCategoryId(category.getId());
	}

	private void createAndMapContent() throws ImportException {
		process.setId(ilcdProcess.getId());
		process.setName(Strings.cut(ilcdProcess.getName(), 254));
		process.setDescription(ilcdProcess.getComment());

		mapTime();
		mapGeography();
		mapTechnology();

		AdminInfo adminInfo = new AdminInfo();
		adminInfo.setId(process.getId());
		mapPublication(adminInfo);
		mapDataEntry(adminInfo);
		mapDataGenerator(adminInfo);
		mapComissionerAndGoal(adminInfo);
		saveInDatabase(adminInfo);

		ModelingAndValidation mav = new ModelingAndValidation();
		mav.setId(process.getId());
		mapLciMethod(mav);
		mapRepresentativeness(mav);
		mapReviews(mav);
		saveInDatabase(mav);

		ProcessParameterConversion paramConv = new ProcessParameterConversion(
				process, database);
		paramConv.run(ilcdProcess);
		ProcessExchanges.mapFrom(dataStore, ilcdProcess).withFlowMap(flowMap)
				.to(database, process);
	}

	private void mapTime() throws ImportException {
		ProcessTime processTime = new ProcessTime(ilcdProcess.getTime());
		Time time = processTime.create(process);
		saveInDatabase(time);
	}

	private void mapGeography() throws ImportException {
		Geography iGeography = ilcdProcess.getGeography();
		if (iGeography != null) {

			if (iGeography.getLocation() != null
					&& iGeography.getLocation().getLocation() != null) {
				String locationCode = iGeography.getLocation().getLocation();
				try {
					String locationId = KeyGen.get(locationCode);

					// find a location
					Location location = database.createDao(Location.class)
							.getForId(locationId);

					// create a new location
					if (location == null) {
						location = new Location();
						location.setCode(locationCode);
						location.setId(locationId);
						location.setName(locationCode);
						database.createDao(Location.class).insert(location);
					}

					process.setLocation(location);
				} catch (Exception e) {
					throw new ImportException(e);
				}
			}

			// comment
			if (iGeography.getLocation() != null) {
				process.setGeographyComment(LangString.getFreeText(iGeography
						.getLocation().getDescription()));
			}

		}
	}

	private void mapTechnology() throws ImportException {
		Technology technology = new Technology();
		technology.setId(process.getId());
		org.openlca.ilcd.processes.Technology iTechnology = ilcdProcess
				.getTechnology();
		if (iTechnology != null) {
			technology.setDescription(LangString.getFreeText(iTechnology
					.getTechnologyDescriptionAndIncludedProcesses()));
		}
		saveInDatabase(technology);
	}

	private void mapPublication(AdminInfo adminInfo) {
		Publication iPublication = ilcdProcess.getPublication();
		if (iPublication != null) {

			// data set owner
			DataSetReference ownerRef = iPublication
					.getReferenceToOwnershipOfDataSet();
			if (ownerRef != null)
				adminInfo.setDataSetOwner(fetchActor(ownerRef));

			// publication
			DataSetReference publicationRef = iPublication
					.getReferenceToUnchangedRepublication();
			if (publicationRef != null)
				adminInfo.setPublication(fetchSource(publicationRef));

			// access and use restrictions
			adminInfo.setAccessAndUseRestrictions(LangString
					.getFreeText(iPublication.getAccessRestrictions()));

			// version
			adminInfo.setVersion(iPublication.getDataSetVersion());

			// copyright
			if (iPublication.isCopyright() != null) {
				adminInfo.setCopyright(iPublication.isCopyright());
			}

		}
	}

	private void mapDataEntry(AdminInfo adminInfo) {
		DataEntry iEntry = ilcdProcess.getDataEntry();
		if (iEntry != null) {

			// last change && creation date
			if (iEntry.getTimeStamp() != null) {
				Date tStamp = iEntry.getTimeStamp().toGregorianCalendar()
						.getTime();
				adminInfo.setCreationDate(tStamp);
				adminInfo.setLastChange(tStamp);
			}

			// data documentor
			if (iEntry.getReferenceToPersonOrEntityEnteringTheData() != null) {
				Actor documentor = fetchActor(iEntry
						.getReferenceToPersonOrEntityEnteringTheData());
				adminInfo.setDataDocumentor(documentor);
			}
		}
	}

	private void mapDataGenerator(AdminInfo adminInfo) {
		if (ilcdProcess.getDataGenerator() != null) {
			List<DataSetReference> refs = ilcdProcess.getDataGenerator()
					.getReferenceToPersonOrEntityGeneratingTheDataSet();
			if (refs != null && !refs.isEmpty()) {
				DataSetReference generatorRef = refs.get(0);
				adminInfo.setDataGenerator(fetchActor(generatorRef));
			}
		}
	}

	private void mapComissionerAndGoal(AdminInfo adminInfo) {
		if (ilcdProcess.getCommissionerAndGoal() != null) {
			CommissionerAndGoal comAndGoal = ilcdProcess
					.getCommissionerAndGoal();
			String intendedApp = LangString.getFreeText(comAndGoal
					.getIntendedApplications());
			adminInfo.setIntendedApplication(intendedApp);
			String project = LangString.getLabel(comAndGoal.getProject());
			adminInfo.setProject(project);
		}
	}

	private void mapLciMethod(ModelingAndValidation mav) {

		if (ilcdProcess.getProcessType() != null) {
			switch (ilcdProcess.getProcessType()) {
			case UNIT_PROCESS_BLACK_BOX:
				process.setProcessType(ProcessType.UnitProcess);
				break;
			case UNIT_PROCESS_SINGLE_OPERATION:
				process.setProcessType(ProcessType.UnitProcess);
				break;
			default:
				process.setProcessType(ProcessType.LCI_Result);
				break;
			}
		}

		LCIMethod iMethod = ilcdProcess.getLciMethod();
		if (iMethod != null) {
			String lciPrinciple = LangString.getFreeText(iMethod
					.getDeviationsFromLCIMethodPrinciple());
			mav.setLCIMethod(lciPrinciple);
			mav.setModelingConstants(LangString.getFreeText(iMethod
					.getModellingConstants()));
			process.setAllocationMethod(getAllocation(iMethod));
		}

	}

	private AllocationMethod getAllocation(LCIMethod iMethod) {
		List<LCIMethodApproach> approaches = iMethod.getLCIMethodApproaches();
		if (approaches == null || approaches.isEmpty())
			return null;
		for (LCIMethodApproach app : approaches) {
			switch (app) {
			case ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT:
				return AllocationMethod.Causal;
			case ALLOCATION_MARKET_VALUE:
				return AllocationMethod.Economic;
			case ALLOCATION_PHYSICAL_CAUSALITY:
				return AllocationMethod.Physical;
			default:
				continue;
			}
		}
		return null;
	}

	private void mapRepresentativeness(ModelingAndValidation mav) {
		Representativeness iRepresentativeness = ilcdProcess
				.getRepresentativeness();
		if (iRepresentativeness != null) {

			// completeness
			mav.setDataCompleteness(LangString.getFreeText(iRepresentativeness
					.getDataCutOffAndCompletenessPrinciples()));

			// data selection
			mav.setDataSelection(LangString.getFreeText(iRepresentativeness
					.getDataSelectionAndCombinationPrinciples()));

			// data treatment
			mav.setDataTreatment(LangString.getFreeText(iRepresentativeness
					.getDataTreatmentAndExtrapolationsPrinciples()));

			// sampling procedure
			mav.setSampling(LangString.getFreeText(iRepresentativeness
					.getSamplingProcedure()));

			// data collection period
			mav.setDataCollectionPeriod(LangString.getLabel(iRepresentativeness
					.getDataCollectionPeriod()));

			// data sources
			for (DataSetReference sourceRef : iRepresentativeness
					.getReferenceToDataSource()) {
				Source source = fetchSource(sourceRef);
				if (source != null)
					mav.add(source);
			}

		}
	}

	private void mapReviews(ModelingAndValidation mav) {
		if (!ilcdProcess.getReviews().isEmpty()) {
			Review iReview = ilcdProcess.getReviews().get(0);
			// reviewer
			if (!iReview.getReferenceToNameOfReviewerAndInstitution().isEmpty()) {
				DataSetReference ref = iReview
						.getReferenceToNameOfReviewerAndInstitution().get(0);
				mav.setReviewer(fetchActor(ref));
			}
			// review details
			mav.setDataSetOtherEvaluation(LangString.getFreeText(iReview
					.getReviewDetails()));
		}
	}

	private Actor fetchActor(DataSetReference reference) {
		if (reference == null)
			return null;
		try {
			ContactImport contactImport = new ContactImport(dataStore, database);
			return contactImport.run(reference.getUuid());
		} catch (Exception e) {
			log.warn("Failed to get contact {} referenced from process {}",
					reference.getUuid(), process.getId());
			return null;
		}
	}

	private Source fetchSource(DataSetReference reference) {
		if (reference == null)
			return null;
		try {
			SourceImport sourceImport = new SourceImport(dataStore, database);
			return sourceImport.run(reference.getUuid());
		} catch (Exception e) {
			log.warn("Failed to get source {} referenced from process {}",
					reference.getUuid(), process.getId());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void saveInDatabase(T obj) throws ImportException {
		try {
			Class<T> clazz = (Class<T>) obj.getClass();
			database.createDao(clazz).insert(obj);
		} catch (Exception e) {
			String message = String.format(
					"Save operation failed in process %s.", process.getId());
			throw new ImportException(message, e);
		}
	}

}
