package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScope;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.InventoryMethod;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.DQSystems;
import org.openlca.util.Strings;

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
		var process = imp.db().get(Process.class, Processes.getUUID(ds));
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
		process.refId = Processes.getUUID(ds);
		process.name = Strings.cut(
				Processes.getFullName(ds, imp.lang()), 2024);
		Import.mapVersionInfo(ds, process);
		var info = Processes.getDataSetInfo(ds);
		if (info != null) {
			process.description = imp.str(info.getComment());
		}

		if (process.documentation == null) {
			process.documentation = new ProcessDoc();
		}
		mapDocumentation(process.documentation);
		mapCompleteness(process.documentation);

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

	private void mapDocumentation(ProcessDoc doc) {
		new ProcessTime(Processes.getTime(ds), imp).map(doc);
		mapGeography(doc);
		mapTechnology(doc);
		mapPublication(doc);
		mapDataEntry(doc);
		mapDataGenerator(doc);
		mapGoal(doc);
		mapInventoryMethod(doc);
		mapRepresentativeness(doc);
		mapComplianceDeclarations(doc);
		mapReviews(doc);
		addSources(doc);
	}

	private void mapGeography(ProcessDoc doc) {
		var loc = Processes.getLocation(ds);
		if (loc == null)
			return;
		doc.geography = imp.str(loc.getDescription());
		process.location = imp.cache.locationOf(loc.getCode());
	}

	private void mapTechnology(ProcessDoc doc) {
		var tech = Processes.getTechnology(ds);
		if (tech != null) {
			doc.technology = imp.str(tech.getDescription());
		}
	}

	private void mapPublication(ProcessDoc doc) {
		var pub = Processes.getPublication(ds);
		if (pub == null)
			return;
		doc.dataOwner = fetchActor(pub.getOwner());
		doc.publication = fetchSource(pub.getRepublication());
		doc.accessRestrictions = imp.str(pub.getAccessRestrictions());
		if (pub.getCopyright() != null) {
			doc.copyright = pub.getCopyright();
		}
	}

	private void mapDataEntry(ProcessDoc doc) {
		var entry = Processes.getDataEntry(ds);
		if (entry == null)
			return;
		doc.dataDocumentor = fetchActor(entry.getDocumentor());
		if (entry.getTimeStamp() != null) {
			doc.creationDate = entry.getTimeStamp()
					.toGregorianCalendar()
					.getTime();
		}
	}

	private void mapDataGenerator(ProcessDoc doc) {
		var gen = Processes.getDataGenerator(ds);
		if (gen == null || gen.getContacts().isEmpty())
			return;
		doc.dataGenerator = fetchActor(gen.getContacts().get(0));
	}

	private void mapGoal(ProcessDoc doc) {
		var goal = Processes.getCommissionerAndGoal(ds);
		if (goal == null)
			return;
		doc.intendedApplication = imp.str(goal.getIntendedApplications());
		doc.project = imp.str(goal.getProject());
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
			doc.inventoryMethod = imp.str(method.getPrincipleDeviations());
			doc.modelingConstants = imp.str(method.getConstants());
			process.defaultAllocationMethod = getAllocation(method);
		}
	}

	private AllocationMethod getAllocation(InventoryMethod m) {
		var approaches = m.getApproaches();
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
		doc.dataCompleteness = imp.str(
				r.getCompleteness(), r.getCompletenessComment());
		doc.dataSelection = imp.str(
				r.getDataSelection(), r.getDataSelectionComment());
		doc.dataTreatment = imp.str(
				r.getDataTreatment(),
				r.getDatatTreatmentComment(),
				r.getUncertaintyAdjustments());
		doc.samplingProcedure = imp.str(r.getSamplingProcedure());
		doc.dataCollectionPeriod = imp.str(r.getDataCollectionPeriod());
		doc.useAdvice = imp.str(r.getUseAdvice());
	}

	private void mapComplianceDeclarations(ProcessDoc doc) {
		for (var c : Processes.getComplianceDeclarations(ds)) {
			var target = new ComplianceDeclaration();
			target.system = fetchSource(c.getSystem());
			doc.complianceDeclarations.add(target);
			if (c.getApproval() != null) {
				target.aspects.put(
						"Overall compliance", c.getApproval().value());
			}
			if (c.getNomenclature() != null) {
				target.aspects.put(
						"Nomenclature compliance", c.getNomenclature().value());
			}
			if (c.getMethod() != null) {
				target.aspects.put(
						"Methodological compliance", c.getMethod().value());
			}
			if (c.getReview() != null) {
				target.aspects.put(
						"Review compliance", c.getReview().value());
			}
			if (c.getDocumentation() != null) {
				target.aspects.put(
						"Documentation compliance", c.getDocumentation().value());
			}
			if (c.getQuality() != null) {
				target.aspects.put(
						"Quality compliance", c.getQuality().value());
			}
		}
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
			rev.type = r.getType() != null
					? r.getType().value()
					: null;
			rev.details = imp.str(r.getDetails());
			rev.report = fetchSource(r.getReport());
			for (var ref : r.getReviewers()) {
				var reviewer = fetchActor(ref);
				if (reviewer != null) {
					rev.reviewers.add(reviewer);
				}
			}

			for (var s : r.getScopes()) {
				if (s.getName() == null)
					continue;
				var scope = new ReviewScope(s.getName().value());
				rev.scopes.put(scope);
				for (var m : s.getMethods()) {
					if (m.getName() == null)
						continue;
					scope.methods.add(m.getName().value());
				}
			}

			for (var a : r.getIndicators()) {
				if (a.getName() == null || a.getValue() == null)
					continue;
				rev.assessment.put(a.getName().value(), a.getValue().value());
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
		return ref != null && ref.getUUID() != null
				? ContactImport.get(imp, ref.getUUID())
				: null;
	}

	private Source fetchSource(Ref ref) {
		return ref != null && ref.getUUID() != null
				? SourceImport.get(imp, ref.getUUID())
				: null;
	}

	private void mapCompleteness(ProcessDoc doc) {
		var c = Processes.getCompleteness(ds);
		if (c == null)
			return;
		var target = doc.flowCompleteness;
		if (c.getProductCompleteness() != null) {
			target.put("Product model", c.getProductCompleteness().value());
		}
		for (var e : c.getEntries()) {
			if (e.getImpact() == null || e.getValue() == null)
				continue;
			target.put(e.getImpact().value(), e.getValue().value());
		}
	}

}
