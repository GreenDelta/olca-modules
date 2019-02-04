package org.openlca.io.ilcd.input;

import java.util.Date;
import java.util.List;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.util.DQSystems;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ImportConfig config;
	private ProcessExchanges exchanges;
	private ProcessBag ilcdProcess;
	private Process process;

	public ProcessImport(ImportConfig config, ProviderLinker linker) {
		this.config = config;
		this.exchanges = new ProcessExchanges(config, linker);
	}

	public Process run(org.openlca.ilcd.processes.Process process)
			throws ImportException {
		this.ilcdProcess = new ProcessBag(process, config.langs);
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
		ilcdProcess = new ProcessBag(iProcess, config.langs);
		return createNew();
	}

	private Process findExisting(String processId) throws ImportException {
		try {
			ProcessDao dao = new ProcessDao(config.db);
			return dao.getForRefId(processId);
		} catch (Exception e) {
			String message = String.format("Search for process %s failed.",
					processId);
			throw new ImportException(message, e);
		}
	}

	private Process createNew() throws ImportException {
		try {
			process = new Process();
			importAndSetCategory();
			createAndMapContent();
			saveInDatabase(process);
			return process;
		} catch (Exception e) {
			throw new ImportException(e);
		}
	}

	private org.openlca.ilcd.processes.Process tryGetProcess(String processId)
			throws ImportException {
		try {
			org.openlca.ilcd.processes.Process iProcess = config.store.get(
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
		CategoryImport categoryImport = new CategoryImport(config,
				ModelType.PROCESS);
		Category category = categoryImport.run(ilcdProcess.getSortedClasses());
		process.category = category;
	}

	private void createAndMapContent() throws ImportException {
		process.refId = ilcdProcess.getId();
		process.name = Strings.cut(ilcdProcess.getName(), 2024);
		process.description = ilcdProcess.getComment();
		ProcessDocumentation doc = mapDocumentation();
		process.documentation = doc;
		ProcessParameterConversion paramConv = new ProcessParameterConversion(
				process, config);
		paramConv.run(ilcdProcess);
		exchanges.map(ilcdProcess, process);
		for (Exchange e : process.exchanges) {
			if (e.dqEntry == null)
				continue;
			process.exchangeDqSystem = new DQSystemDao(config.db)
					.insert(DQSystems.ecoinvent());
			break;
		}
	}

	private ProcessDocumentation mapDocumentation() throws ImportException {
		ProcessDocumentation doc = new ProcessDocumentation();
		ProcessTime processTime = new ProcessTime(ilcdProcess.getTime(),
				config);
		processTime.map(doc);
		mapGeography(doc);
		mapTechnology(doc);
		mapPublication(doc);
		mapDataEntry(doc);
		mapDataGenerator(doc);
		mapComissionerAndGoal(doc);
		mapLciMethod(doc);
		mapRepresentativeness(doc);
		mapReviews(doc);
		addSources(doc);
		return doc;
	}

	private void mapGeography(ProcessDocumentation doc) throws ImportException {
		Geography iGeography = ilcdProcess.getGeography();
		if (iGeography == null || iGeography.location == null)
			return;
		doc.geography = LangString.getFirst(iGeography.location.description,
				config.langs);
		if (iGeography.location.code == null)
			return;
		String code = iGeography.location.code;
		Location location = Locations.getOrCreate(code, config);
		process.location = location;
	}

	private void mapTechnology(ProcessDocumentation doc) {
		org.openlca.ilcd.processes.Technology iTechnology = ilcdProcess
				.getTechnology();
		if (iTechnology != null) {
			doc.technology = LangString.getFirst(
					iTechnology.description,
					config.langs);
		}
	}

	private void mapPublication(ProcessDocumentation doc) {
		Publication iPublication = ilcdProcess.getPublication();
		if (iPublication != null) {

			// data set owner
			Ref ownerRef = iPublication.owner;
			if (ownerRef != null)
				doc.dataSetOwner = fetchActor(ownerRef);

			// publication
			Ref publicationRef = iPublication.republication;
			if (publicationRef != null)
				doc.publication = fetchSource(publicationRef);

			// access and use restrictions
			doc.restrictions = LangString.getFirst(
					iPublication.accessRestrictions, config.langs);

			// version
			process.version = Version.fromString(
					iPublication.version).getValue();

			// copyright
			if (iPublication.copyright != null) {
				doc.copyright = iPublication.copyright;
			}

		}
	}

	private void mapDataEntry(ProcessDocumentation doc) {
		DataEntry iEntry = ilcdProcess.getDataEntry();
		if (iEntry == null)
			return;
		if (iEntry.timeStamp != null) {
			Date tStamp = iEntry.timeStamp.toGregorianCalendar().getTime();
			doc.creationDate = tStamp;
			if (tStamp != null)
				process.lastChange = tStamp.getTime();
		}
		if (iEntry.documentor != null) {
			Actor documentor = fetchActor(
					iEntry.documentor);
			doc.dataDocumentor = documentor;
		}
	}

	private void mapDataGenerator(ProcessDocumentation doc) {
		if (ilcdProcess.getDataGenerator() != null) {
			List<Ref> refs = ilcdProcess
					.getDataGenerator().contacts;
			if (refs != null && !refs.isEmpty()) {
				Ref generatorRef = refs.get(0);
				doc.dataGenerator = fetchActor(generatorRef);
			}
		}
	}

	private void mapComissionerAndGoal(ProcessDocumentation doc) {
		if (ilcdProcess.getCommissionerAndGoal() != null) {
			CommissionerAndGoal comAndGoal = ilcdProcess
					.getCommissionerAndGoal();
			String intendedApp = LangString.getFirst(
					comAndGoal.intendedApplications, config.langs);
			doc.intendedApplication = intendedApp;
			String project = LangString.getFirst(comAndGoal.project,
					config.langs);
			doc.project = project;
		}
	}

	private void mapLciMethod(ProcessDocumentation doc) {
		if (ilcdProcess.getProcessType() != null) {
			switch (ilcdProcess.getProcessType()) {
			case UNIT_PROCESS_BLACK_BOX:
				process.processType = ProcessType.UNIT_PROCESS;
				break;
			case UNIT_PROCESS:
				process.processType = ProcessType.UNIT_PROCESS;
				break;
			default:
				process.processType = ProcessType.LCI_RESULT;
				break;
			}
		}
		Method iMethod = ilcdProcess.getLciMethod();
		if (iMethod != null) {
			String lciPrinciple = LangString.getFirst(
					iMethod.principleComment,
					config.langs);
			doc.inventoryMethod = lciPrinciple;
			doc.modelingConstants = LangString.getFirst(
					iMethod.constants, config.langs);
			process.defaultAllocationMethod = getAllocation(iMethod);
		}
	}

	private AllocationMethod getAllocation(Method iMethod) {
		List<ModellingApproach> approaches = iMethod.approaches;
		if (approaches == null || approaches.isEmpty())
			return null;
		for (ModellingApproach app : approaches) {
			switch (app) {
			case ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT:
				return AllocationMethod.CAUSAL;
			case ALLOCATION_MARKET_VALUE:
				return AllocationMethod.ECONOMIC;
			case ALLOCATION_PHYSICAL_CAUSALITY:
				return AllocationMethod.PHYSICAL;
			default:
				continue;
			}
		}
		return null;
	}

	private void mapRepresentativeness(ProcessDocumentation doc) {
		Representativeness repr = ilcdProcess.getRepresentativeness();
		if (repr == null)
			return;
		doc.completeness = LangString.getFirst(
				repr.completeness, config.langs);
		doc.dataSelection = LangString.getFirst(
				repr.dataSelection, config.langs);
		doc.dataTreatment = LangString.getFirst(
				repr.dataTreatment, config.langs);
		doc.sampling = LangString.getFirst(repr.samplingProcedure,
				config.langs);
		doc.dataCollectionPeriod = LangString.getFirst(
				repr.dataCollectionPeriod, config.langs);
	}

	private void addSources(ProcessDocumentation doc) {
		List<Ref> refs = ilcdProcess.getAllSources();
		for (Ref ref : refs) {
			if (ref == null)
				continue;
			Source source = fetchSource(ref);
			if (source == null || doc.sources.contains(source))
				continue;
			doc.sources.add(source);
		}

	}

	private void mapReviews(ProcessDocumentation doc) {
		if (ilcdProcess.getReviews().isEmpty())
			return;
		Review review = ilcdProcess.getReviews().get(0);
		if (!review.reviewers.isEmpty()) {
			Ref ref = review.reviewers.get(0);
			doc.reviewer = fetchActor(ref);
		}
		doc.reviewDetails = LangString.getFirst(review.details, config.langs);
		String dq = DQEntry.get(review);
		if (dq != null) {
			DQSystem dqs = DQSystems.ilcd(config.db);
			if (dqs != null) {
				process.dqSystem = dqs;
				process.dqEntry = dq;
			}
		}
	}

	private Actor fetchActor(Ref reference) {
		if (reference == null)
			return null;
		try {
			ContactImport contactImport = new ContactImport(config);
			return contactImport.run(reference.uuid);
		} catch (Exception e) {
			log.warn("Failed to get contact {} referenced from process {}",
					reference.uuid, process.refId);
			return null;
		}
	}

	private Source fetchSource(Ref reference) {
		if (reference == null)
			return null;
		try {
			SourceImport sourceImport = new SourceImport(config);
			return sourceImport.run(reference.uuid);
		} catch (Exception e) {
			log.warn("Failed to get source {} referenced from process {}",
					reference.uuid, process.refId);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractEntity> void saveInDatabase(T obj)
			throws ImportException {
		try {
			Class<T> clazz = (Class<T>) obj.getClass();
			Daos.base(config.db, clazz).insert(obj);
		} catch (Exception e) {
			String message = String.format(
					"Save operation failed in process %s.", process.refId);
			throw new ImportException(message, e);
		}
	}

}
