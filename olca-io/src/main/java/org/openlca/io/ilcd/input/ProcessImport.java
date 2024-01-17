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
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScope;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.InventoryMethod;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.DQSystems;
import org.openlca.util.Strings;

import java.util.Date;

public class ProcessImport {

	private final Import imp;
	private final org.openlca.ilcd.processes.Process ds;
	private final ProcessExchanges exchanges;
	private Process process;

	public ProcessImport(Import imp, org.openlca.ilcd.processes.Process ds) {
		this.imp = imp;
		this.ds = ds;
		this.exchanges = new ProcessExchanges(imp);
	}

	public Process run() {
		var process = imp.db().get(Process.class, ds.getUUID());
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
		return new ProcessImport(imp, ds).run();
	}

	private Process createNew() {
		process = new Process();
		String[] path = Categories.getPath(ds);
		process.category = new CategoryDao(imp.db())
				.sync(ModelType.PROCESS, path);
		createAndMapContent();
		org.openlca.util.Processes.fixInternalIds(process);
		process = imp.insert(process);
		imp.providers().pop(process);
		return process;
	}

	private void createAndMapContent() {
		process.refId = ds.getUUID();
		process.name = Strings.cut(
				Processes.getFullName(ds, imp.langOrder()), 2024);
		var info = Processes.getDataSetInfo(ds);
		if (info != null) {
			process.description = imp.str(info.comment);
		}

		process.documentation = mapDocumentation();
		mapCompleteness();

		new ProcessParameterConversion(process, imp).run(ds);
		exchanges.map(ds, process);

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
		new ProcessTime(Processes.getTime(ds), imp).map(doc);
		mapGeography(doc);
		mapTechnology(doc);
		mapPublication(doc);
		mapDataEntry(doc);
		mapDataGenerator(doc);
		mapGoal(doc);
		mapInventoryMethod(doc);
		mapRepresentativeness(doc);
		mapReviews(doc);
		addSources(doc);
		return doc;
	}

	private void mapGeography(ProcessDoc doc) {
		var loc = Processes.getLocation(ds);
		if (loc == null)
			return;
		doc.geography = imp.str(loc.description);
		process.location = imp.cache.locationOf(loc.code);
	}

	private void mapTechnology(ProcessDoc doc) {
		var tech = Processes.getTechnology(ds);
		if (tech != null) {
			doc.technology = imp.str(tech.description);
		}
	}

	private void mapPublication(ProcessDoc doc) {
		var pub = Processes.getPublication(ds);
		if (pub != null) {

			// data set owner
			Ref ownerRef = pub.owner;
			if (ownerRef != null) {
				doc.dataOwner = fetchActor(ownerRef);
			}

			// publication
			Ref publicationRef = pub.republication;
			if (publicationRef != null) {
				doc.publication = fetchSource(publicationRef);
			}

			// access and use restrictions
			doc.accessRestrictions = imp.str(pub.accessRestrictions);

			// version
			process.version = Version.fromString(
					pub.version).getValue();

			// copyright
			if (pub.copyright != null) {
				doc.copyright = pub.copyright;
			}

		}
	}

	private void mapDataEntry(ProcessDoc doc) {
		var entry = Processes.getDataEntry(ds);
		if (entry == null)
			return;
		if (entry.timeStamp != null) {
			Date tStamp = entry.timeStamp.toGregorianCalendar().getTime();
			doc.creationDate = tStamp;
			process.lastChange = tStamp.getTime();
		}
		if (entry.documentor != null) {
			doc.dataDocumentor = fetchActor(entry.documentor);
		}
	}

	private void mapDataGenerator(ProcessDoc doc) {
		var gen = Processes.getDataGenerator(ds);
		if (gen != null) {
			if (!gen.contacts.isEmpty()) {
				doc.dataGenerator = fetchActor(gen.contacts.get(0));
			}
		}
	}

	private void mapGoal(ProcessDoc doc) {
		var goal = Processes.getCommissionerAndGoal(ds);
		if (goal != null) {
			doc.intendedApplication = imp.str(goal.intendedApplications);
			doc.project = imp.str(goal.project);
		}
	}

	private void mapInventoryMethod(ProcessDoc doc) {
		var type = Processes.getProcessType(ds);
		if (type != null) {
			process.processType = switch (type) {
				case UNIT_PROCESS_BLACK_BOX, UNIT_PROCESS -> ProcessType.UNIT_PROCESS;
				default -> ProcessType.LCI_RESULT;
			};
		}
		var method = Processes.getInventoryMethod(ds);
		if (method != null) {
			doc.inventoryMethod = imp.str(method.principleDeviations);
			doc.modelingConstants = imp.str(method.constants);
			process.defaultAllocationMethod = getAllocation(method);
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
					SUBSTITUTION_AVERAGE_MARKET_PRICE_CORRECTION ->
					AllocationMethod.ECONOMIC;
			default -> AllocationMethod.PHYSICAL;
		};
	}

	private void mapRepresentativeness(ProcessDoc doc) {
		var r = Processes.getRepresentativeness(ds);
		if (r == null)
			return;
		doc.dataCompleteness = imp.str(r.completeness);
		doc.dataSelection = imp.str(r.dataSelection);
		doc.dataTreatment = imp.str(r.dataTreatment);
		doc.samplingProcedure = imp.str(r.samplingProcedure);
		doc.dataCollectionPeriod = imp.str(r.dataCollectionPeriod);
	}

	private void addSources(ProcessDoc doc) {
		for (Ref ref : ProcessSources.allOf(ds)) {
			if (ref == null)
				continue;
			Source source = fetchSource(ref);
			if (source == null || doc.sources.contains(source))
				continue;
			doc.sources.add(source);
		}

	}

	private void mapReviews(ProcessDoc doc) {
		for (var r : Processes.getReviews(ds)) {

			var rev = new Review();
			doc.reviews.add(rev);
			rev.type = r.type != null ? r.type.value() : null;
			rev.details = imp.str(r.details);
			rev.report = fetchSource(r.report);
			for (var ref : r.reviewers) {
				var reviewer = fetchActor(ref);
				if (reviewer != null) {
					rev.reviewers.add(reviewer);
				}
			}

			for (var s : r.scopes) {
				if (s.name == null)
					continue;
				var scope = new ReviewScope(s.name.value());
				rev.scopes.add(scope);
				for (var m : s.methods) {
					if (m.name == null)
						continue;
					scope.methods.add(m.name.value());
				}
			}

			// take the first best data quality entry as
			// data quality statement of the dataset
			if (process.dqEntry != null)
				continue;
			var dq = DQEntry.get(r);
			if (dq != null) {
				var dqs = DQSystems.ilcd(imp.db());
				if (dqs != null) {
					process.dqSystem = dqs;
					process.dqEntry = dq;
				}
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
		var c = Processes.getCompleteness(ds);
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
