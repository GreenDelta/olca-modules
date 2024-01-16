package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDoc;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.core.model.doc.Completeness;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.InventoryMethod;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.DQSystems;
import org.openlca.util.Strings;

import java.util.Date;
import java.util.List;

public class ProcessImport {

	private final Import imp;
	private final ProcessExchanges exchanges;
	private ProcessBag ilcdProcess;
	private Process process;

	public ProcessImport(Import imp) {
		this.imp = imp;
		this.exchanges = new ProcessExchanges(imp);
	}

	public Process run(org.openlca.ilcd.processes.Process dataSet) {
		this.ilcdProcess = new ProcessBag(dataSet, imp.langOrder());
		var process = imp.db().get(Process.class, dataSet.getUUID());
		return process != null
			? process
			: createNew();
	}

	public static Process get(Import imp, String id) {
		var process = imp.db().get(Process.class, id);
		if (process != null)
			return process;
		var ds = imp.store().get(org.openlca.ilcd.processes.Process.class, id);
		if (ds == null) {
			imp.log().error("invalid reference in ILCD data set:" +
				" process '" + id + "' does not exist");
			return null;
		}
		return new ProcessImport(imp).run(ds);
	}

	private Process createNew() {
		process = new Process();
		String[] path = Categories.getPath(ilcdProcess.getValue());
		process.category = new CategoryDao(imp.db())
			.sync(ModelType.PROCESS, path);
		createAndMapContent();
		org.openlca.util.Processes.fixInternalIds(process);
		process = imp.insert(process);
		imp.providers().pop(process);
		return process;
	}

	private void createAndMapContent() {
		var dataSet = ilcdProcess.getValue();
		process.refId = dataSet.getUUID();
		process.name = Strings.cut(
			Processes.fullName(dataSet, imp.langOrder()), 2024);
		var info = Processes.getDataSetInfo(dataSet);
		if (info != null) {
			process.description = imp.str(info.comment);
		}

		process.documentation = mapDocumentation();
		mapCompleteness();

		new ProcessParameterConversion(process, imp).run(ilcdProcess);
		exchanges.map(ilcdProcess, process);

		// set the DQ system for exchanges if needed
		for (var e : process.exchanges) {
			if (Strings.notEmpty(e.dqEntry)) {
				process.exchangeDqSystem = DQSystems.ecoinvent(imp.db());
				break;
			}
		}
	}

	private ProcessDoc mapDocumentation() {
		var doc = new ProcessDoc();
		var processTime = new ProcessTime(ilcdProcess.getTime(), imp);
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

	private void mapGeography(ProcessDoc doc) {
		var loc = Processes.getLocation(ilcdProcess.getValue());
		if (loc == null)
			return;
		doc.geography = imp.str(loc.description);
		process.location = imp.cache.locationOf(loc.code);
	}

	private void mapTechnology(ProcessDoc doc) {
		var iTech = ilcdProcess.getTechnology();
		if (iTech != null) {
			doc.technology = imp.str(iTech.description);
		}
	}

	private void mapPublication(ProcessDoc doc) {
		var iPub = ilcdProcess.getPublication();
		if (iPub != null) {

			// data set owner
			Ref ownerRef = iPub.owner;
			if (ownerRef != null) {
				doc.dataOwner = fetchActor(ownerRef);
			}

			// publication
			Ref publicationRef = iPub.republication;
			if (publicationRef != null) {
				doc.publication = fetchSource(publicationRef);
			}

			// access and use restrictions
			doc.accessRestrictions = imp.str(iPub.accessRestrictions);

			// version
			process.version = Version.fromString(
				iPub.version).getValue();

			// copyright
			if (iPub.copyright != null) {
				doc.copyright = iPub.copyright;
			}

		}
	}

	private void mapDataEntry(ProcessDoc doc) {
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

	private void mapDataGenerator(ProcessDoc doc) {
		if (ilcdProcess.getDataGenerator() != null) {
			List<Ref> refs = ilcdProcess.getDataGenerator().contacts;
			if (!refs.isEmpty()) {
				Ref generatorRef = refs.get(0);
				doc.dataGenerator = fetchActor(generatorRef);
			}
		}
	}

	private void mapComissionerAndGoal(ProcessDoc doc) {
		if (ilcdProcess.getCommissionerAndGoal() != null) {
			var cag = ilcdProcess.getCommissionerAndGoal();
			doc.intendedApplication = imp.str(cag.intendedApplications);
			doc.project = imp.str(cag.project);
		}
	}

	private void mapLciMethod(ProcessDoc doc) {
		if (ilcdProcess.getProcessType() != null) {
			process.processType = switch (ilcdProcess.getProcessType()) {
				case UNIT_PROCESS_BLACK_BOX, UNIT_PROCESS -> ProcessType.UNIT_PROCESS;
				default -> ProcessType.LCI_RESULT;
			};
		}
		var iMethod = ilcdProcess.getLciMethod();
		if (iMethod != null) {
			doc.inventoryMethod = imp.str(iMethod.principleDeviations);
			doc.modelingConstants = imp.str(iMethod.constants);
			process.defaultAllocationMethod = getAllocation(iMethod);
		}
	}

	private AllocationMethod getAllocation(InventoryMethod m) {
		var approaches = m.approaches;
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

	private void mapRepresentativeness(ProcessDoc doc) {
		var r = ilcdProcess.getRepresentativeness();
		if (r == null)
			return;
		doc.dataCompleteness = imp.str(r.completeness);
		doc.dataSelection = imp.str(r.dataSelection);
		doc.dataTreatment = imp.str(r.dataTreatment);
		doc.samplingProcedure = imp.str(r.samplingProcedure);
		doc.dataCollectionPeriod = imp.str(r.dataCollectionPeriod);
	}

	private void addSources(ProcessDoc doc) {
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

	private void mapReviews(ProcessDoc doc) {
		if (ilcdProcess.getReviews().isEmpty())
			return;
		var review = ilcdProcess.getReviews().get(0);
		if (!review.reviewers.isEmpty()) {
			Ref ref = review.reviewers.get(0);
			doc.reviewer = fetchActor(ref);
		}
		doc.reviewDetails = imp.str(review.details);
		var dq = DQEntry.get(review);
		if (dq != null) {
			var dqs = DQSystems.ilcd(imp.db());
			if (dqs != null) {
				process.dqSystem = dqs;
				process.dqEntry = dq;
			}
		}
	}

	private Actor fetchActor(Ref ref) {
		return ref != null
			? ContactImport.get(imp, ref.uuid)
			: null;
	}

	private Source fetchSource(Ref ref) {
		return ref != null
			? SourceImport.get(imp, ref.uuid)
			: null;
	}

	private void mapCompleteness() {
		var c = Processes.getCompleteness(ilcdProcess.getValue());
		if (c == null)
			return;
		var target = new Completeness();
		if (c.productCompleteness != null) {
			target.put("Product model", c.productCompleteness.value());
		}
		for (var e : c.entries) {
			if (e.impact == null || e.value == null)
				continue;
			target.put(e.impact.value(), e.value.value());
		}
		if (!target.isEmpty()) {
			target.writeTo(process);
		}
	}

}
