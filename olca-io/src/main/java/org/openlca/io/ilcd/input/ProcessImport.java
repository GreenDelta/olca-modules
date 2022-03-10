package org.openlca.io.ilcd.input;

import java.util.Date;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.DQSystems;
import org.openlca.util.Strings;

public class ProcessImport {

	private final ImportConfig config;
	private final ProcessExchanges exchanges;
	private ProcessBag ilcdProcess;
	private Process process;

	public ProcessImport(ImportConfig config) {
		this.config = config;
		this.exchanges = new ProcessExchanges(config);
	}

	public Process run(org.openlca.ilcd.processes.Process dataSet) {
		this.ilcdProcess = new ProcessBag(dataSet, config.langOrder());
		var process = config.db().get(Process.class, dataSet.getUUID());
		return process != null
			? process
			: createNew();
	}

	public static Process get(ImportConfig config, String id) {
		var process = config.db().get(Process.class, id);
		if (process != null)
			return process;
		var dataSet = config.store().get(
			org.openlca.ilcd.processes.Process.class, id);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" process '" + id + "' does not exist");
			return null;
		}
		return new ProcessImport(config).run(dataSet);
	}

	private Process createNew() {
		process = new Process();
		String[] path = Categories.getPath(ilcdProcess.getValue());
		process.category = new CategoryDao(config.db())
			.sync(ModelType.PROCESS, path);
		createAndMapContent();
		process = config.insert(process);
		config.providers().pop(process);
		return process;
	}

	private void createAndMapContent() {
		var dataSet = ilcdProcess.getValue();
		process.refId = dataSet.getUUID();
		process.name = Strings.cut(
			Processes.fullName(dataSet, config.langOrder()), 2024);
		var info = Processes.getDataSetInfo(dataSet);
		if (info != null) {
			process.description = config.str(info.comment);
		}

		process.documentation = mapDocumentation();

		new ProcessParameterConversion(process, config)
			.run(ilcdProcess);
		exchanges.map(ilcdProcess, process);

		// set the DQ system for exchanges if needed
		for (var e : process.exchanges) {
			if (Strings.notEmpty(e.dqEntry)) {
				process.exchangeDqSystem = DQSystems.ecoinvent(config.db());
				break;
			}
		}
	}

	private ProcessDocumentation mapDocumentation() {
		var doc = new ProcessDocumentation();
		var processTime = new ProcessTime(ilcdProcess.getTime(), config);
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

	private void mapGeography(ProcessDocumentation doc) {
		var loc = Processes.getLocation(ilcdProcess.getValue());
		if (loc == null)
			return;
		doc.geography = config.str(loc.description);
		process.location = config.locationOf(loc.code);
	}

	private void mapTechnology(ProcessDocumentation doc) {
		var iTech = ilcdProcess.getTechnology();
		if (iTech != null) {
			doc.technology = config.str(iTech.description);
		}
	}

	private void mapPublication(ProcessDocumentation doc) {
		var iPub = ilcdProcess.getPublication();
		if (iPub != null) {

			// data set owner
			Ref ownerRef = iPub.owner;
			if (ownerRef != null) {
				doc.dataSetOwner = fetchActor(ownerRef);
			}

			// publication
			Ref publicationRef = iPub.republication;
			if (publicationRef != null) {
				doc.publication = fetchSource(publicationRef);
			}

			// access and use restrictions
			doc.restrictions = config.str(iPub.accessRestrictions);

			// version
			process.version = Version.fromString(
				iPub.version).getValue();

			// copyright
			if (iPub.copyright != null) {
				doc.copyright = iPub.copyright;
			}

		}
	}

	private void mapDataEntry(ProcessDocumentation doc) {
		var iEntry = ilcdProcess.getDataEntry();
		if (iEntry == null)
			return;
		if (iEntry.timeStamp != null) {
			Date tStamp = iEntry.timeStamp.toGregorianCalendar().getTime();
			doc.creationDate = tStamp;
			process.lastChange = tStamp.getTime();
		}
		if (iEntry.documentor != null) {
			doc.dataDocumentor = fetchActor(iEntry.documentor);
		}
	}

	private void mapDataGenerator(ProcessDocumentation doc) {
		if (ilcdProcess.getDataGenerator() != null) {
			List<Ref> refs = ilcdProcess.getDataGenerator().contacts;
			if (!refs.isEmpty()) {
				Ref generatorRef = refs.get(0);
				doc.dataGenerator = fetchActor(generatorRef);
			}
		}
	}

	private void mapComissionerAndGoal(ProcessDocumentation doc) {
		if (ilcdProcess.getCommissionerAndGoal() != null) {
			var cag = ilcdProcess.getCommissionerAndGoal();
			doc.intendedApplication = config.str(cag.intendedApplications);
			doc.project = config.str(cag.project);
		}
	}

	private void mapLciMethod(ProcessDocumentation doc) {
		if (ilcdProcess.getProcessType() != null) {
			process.processType = switch (ilcdProcess.getProcessType()) {
				case UNIT_PROCESS_BLACK_BOX, UNIT_PROCESS -> ProcessType.UNIT_PROCESS;
				default -> ProcessType.LCI_RESULT;
			};
		}
		var iMethod = ilcdProcess.getLciMethod();
		if (iMethod != null) {
			doc.inventoryMethod = config.str(iMethod.principleComment);
			doc.modelingConstants = config.str(iMethod.constants);
			process.defaultAllocationMethod = getAllocation(iMethod);
		}
	}

	private AllocationMethod getAllocation(Method iMethod) {
		var approaches = iMethod.approaches;
		if (approaches.isEmpty())
			return null;
		var first = approaches.get(0);
		return switch (first) {
			case ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT,
				ALLOCATION_MARGINAL_CAUSALITY,
				ALLOCATION_ABILITY_TO_BEAR,
				ALLOCATION_ELEMENT_CONTENT -> AllocationMethod.CAUSAL;
			case ALLOCATION_MARKET_VALUE,
				SUBSTITUTION_AVERAGE_MARKET_PRICE_CORRECTION -> AllocationMethod.ECONOMIC;
			default -> AllocationMethod.PHYSICAL;
		};
	}

	private void mapRepresentativeness(ProcessDocumentation doc) {
		var r = ilcdProcess.getRepresentativeness();
		if (r == null)
			return;
		doc.completeness = config.str(r.completeness);
		doc.dataSelection = config.str(r.dataSelection);
		doc.dataTreatment = config.str(r.dataTreatment);
		doc.sampling = config.str(r.samplingProcedure);
		doc.dataCollectionPeriod = config.str(r.dataCollectionPeriod);
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
		var review = ilcdProcess.getReviews().get(0);
		if (!review.reviewers.isEmpty()) {
			Ref ref = review.reviewers.get(0);
			doc.reviewer = fetchActor(ref);
		}
		doc.reviewDetails = config.str(review.details);
		var dq = DQEntry.get(review);
		if (dq != null) {
			var dqs = DQSystems.ilcd(config.db());
			if (dqs != null) {
				process.dqSystem = dqs;
				process.dqEntry = dq;
			}
		}
	}

	private Actor fetchActor(Ref ref) {
		return ref != null
			? ContactImport.get(config, ref.uuid)
			: null;
	}

	private Source fetchSource(Ref ref) {
		return ref != null
			? SourceImport.get(config, ref.uuid)
			: null;
	}

}
