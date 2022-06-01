package org.openlca.io.openepd.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.core.model.Source;
import org.openlca.io.openepd.EpdDoc;
import org.openlca.io.openepd.EpdOrg;
import org.openlca.io.openepd.EpdPcr;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

public class EpdImport {

	private final IDatabase db;
	private final EpdDoc epd;
	private final MappingModel mapping;
	private final String[] categoryPath;
	private final ImportLog log;

	public EpdImport(IDatabase db, EpdDoc epd, MappingModel mapping) {
		this.db = db;
		this.epd = epd;
		this.mapping = mapping;
		this.categoryPath = categoryOf(epd).orElse(null);
		this.log = new ImportLog();
	}

	public ImportLog log() {
		return log;
	}

	public Epd run() {

		var refFlow = RefFlow.of(db, epd);
		var modules = createModules(refFlow);

		var e = new Epd();
		e.name = epd.productName;
		e.refId = epd.id;
		e.product = refFlow.product();
		e.description = epd.lcaDiscussion;
		e.modules.addAll(modules);
		e.urn = "openEPD:" + epd.id;
		e.category = syncCategory(ModelType.EPD);
		e.lastChange = System.currentTimeMillis();

		e.manufacturer = getActor(epd.manufacturer);
		e.verifier = getActor(epd.verifier);
		e.programOperator = getActor(epd.programOperator);
		e.pcr = getSource(epd.pcr);

		e = db.insert(e);
		log.imported(e);
		return e;
	}

	private Category syncCategory(ModelType type) {
		return categoryPath == null || categoryPath.length == 0
			? null
			: CategoryDao.sync(db, type, categoryPath);
	}

	private Actor getActor(EpdOrg org) {
		if (org == null || Strings.nullOrEmpty(org.name))
			return null;
		var id = Strings.notEmpty(org.ref)
			? KeyGen.get(org.ref)
			: KeyGen.get(org.name);
		var actor = db.get(Actor.class, id);
		if (actor != null)
			return actor;
		actor = Actor.of(org.name);
		actor.refId = id;
		actor.website = org.webDomain;
		actor.description = org.ref;
		actor = db.insert(actor);
		log.imported(actor);
		return actor;
	}

	private Source getSource(EpdPcr pcr) {
		if (pcr == null || Strings.nullOrEmpty(pcr.name))
			return null;
		var id = pcr.id;
		if (Strings.nullOrEmpty(id)) {
			id = Strings.notEmpty(pcr.ref)
				? KeyGen.get(pcr.ref)
				: KeyGen.get(pcr.name);
		}
		var source = db.get(Source.class, id);
		if (source != null)
			return source;
		source = Source.of(pcr.name);
		source.refId = id;
		source.url = pcr.ref;
		source = db.insert(source);
		log.imported(source);
		return source;
	}

	private Collection<EpdModule> createModules(RefFlow refFlow) {

		var modules = new HashMap<String, EpdModule>();
		for (var m : mapping.mappings()) {
			if (m.method() == null
				|| m.epdMethod() == null
				|| m.entries().isEmpty()) {
				continue;
			}

			for (var entry : m.entries()) {
				if (entry.indicator() == null)
					continue;
				for (var scope : m.scopes()) {
					var value = entry.values().get(scope);
					if (value == null)
						continue;

					// get/init the module result
					var fullName = refFlow.name()
						+ " - " + scope
						+ " - " + m.epdMethod().code();
					var mod = modules.computeIfAbsent(fullName, _k -> {
						var modResult = initResult(
							fullName, refFlow, m.method());
						return EpdModule.of(scope, modResult);
					});

					// add the result value
					var impact = ImpactResult.of(
						entry.indicator(), value * entry.factor());
					mod.result.impactResults.add(impact);
				}
			}
		}

		// persist the module results
		for (var mod : modules.values()) {
			var result = db.insert(mod.result);
			log.imported(result);
			mod.result = result;
		}
		return modules.values();
	}

	private Result initResult(String name, RefFlow refFlow, ImpactMethod method) {
		var qRef = refFlow.create();
		var result = Result.of(name);
		result.impactMethod = method;
		result.description = "Imported from openEPD: " + epd.id;
		result.category = syncCategory(ModelType.RESULT);
		result.referenceFlow = qRef;
		result.flowResults.add(qRef);
		return result;
	}

	public static Optional<String[]> categoryOf(EpdDoc epd) {
		if (epd == null || epd.productClasses.isEmpty())
			return Optional.empty();
		String path = null;
		for (var c : epd.productClasses) {
			if (Objects.equals(c.first, "io.cqd.ec3")) {
				path = c.second;
				break;
			}
			if (path == null) {
				path = c.second;
			}
		}
		if (Strings.nullOrEmpty(path))
			return Optional.empty();


		var segments = new ArrayList<String>();
		var word = new StringBuilder();
		Runnable nextWord = () -> {
			if (word.length() == 0)
				return;
			segments.add(word.toString());
			word.setLength(0);
		};

		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			switch (c) {
				case '/', '\\', '>', '<' -> nextWord.run();
				default -> word.append(c);
			}
		}
		nextWord.run();


		return segments.size() > 0
			? Optional.of(segments.toArray(String[]::new))
			: Optional.empty();
	}
}
