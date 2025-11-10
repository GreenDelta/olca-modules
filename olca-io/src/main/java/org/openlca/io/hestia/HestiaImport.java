package org.openlca.io.hestia;

import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.io.hestia.HestiaExchange.Emission;
import org.openlca.io.hestia.HestiaExchange.Input;
import org.openlca.io.hestia.HestiaExchange.Practice;
import org.openlca.io.hestia.HestiaExchange.Product;
import org.openlca.util.KeyGen;

public class HestiaImport {

	private final ImportLog log = new ImportLog();
	private final HestiaClient client;
	private final IDatabase db;
	private final FlowFetch flows;
	private final LocationMap locations;
	private final SourceFetch sources;

	public HestiaImport(HestiaClient client, IDatabase db, FlowMap flowMap) {
		this.client = Objects.requireNonNull(client);
		this.db = Objects.requireNonNull(db);
		this.flows = FlowFetch.of(log, db, flowMap);
		this.locations = LocationMap.of(db);
		this.sources = SourceFetch.of(log, client, db);
	}

	public ImportLog log() {
		return log;
	}

	public Res<Process> importCycle(String cycleId) {
		if (Strings.isBlank(cycleId))
			return Res.error("cycle ID must not be null or empty");

		var refId = KeyGen.get("cycle", cycleId);
		if (db.get(Process.class, refId) != null)
			return Res.error("a mapped process for cycle ID " + cycleId
					+ " already exists: " + refId);

		// fetch the cycle
		var res = client.getCycle(cycleId);
		if (res.isError())
			return res.wrapError("failed to fetch cycle " + cycleId);
		var cycle = res.value();

		// fetch the site
		var site = siteOf(cycle);

		// create and map the process
		try {
			var process = new Process();
			process.documentation = new ProcessDoc();
			process.refId = refId;
			process.name = cycle.name();
			process.description = cycle.description();
			process.location = locations.get(site);
			mapDates(cycle, process);

			var sources = this.sources.get(cycle);
			if (!sources.isEmpty()) {
				process.documentation.sources.addAll(sources);
			}

			mapExchanges(cycle, site, process);

			db.insert(process);
			log.imported(process);

			return Res.ok(process);
		} catch (Exception e) {
			return Res.error("mapping process data failed", e);
		}
	}

	private void mapExchanges(Cycle cycle, Site site, Process process) {

		for (var product : cycle.products()) {
			var prim = product.isPrimary();
			var type = prim
					? FlowType.PRODUCT_FLOW
					: FlowType.WASTE_FLOW;
			exchangeOf(product, site, process, type);

			// we take the "category" of the primary product also as category
			// of the process
			if (prim) {
				mapProcessCategory(product.term(), process);
			}
		}

		for (var input : cycle.inputs()) {
			exchangeOf(input, site, process, FlowType.PRODUCT_FLOW);
		}
		for (var emission : cycle.emissions()) {
			exchangeOf(emission, site, process, FlowType.ELEMENTARY_FLOW);
		}
	}

	private void mapProcessCategory(Term term, Process process) {
		if (term == null)
			return;
		var cat = term.getCategoryName();
		if (Strings.isBlank(cat))
			return;
		process.category = CategoryDao.sync(db, ModelType.PROCESS, cat);
	}

	private Site siteOf(Cycle cycle) {
		var ref = cycle.site();
		if (ref == null || Strings.isBlank(ref.id()))
			return null;
		var res = client.getSite(ref.id());
		if (res.isError()) {
			log.error(res.error());
			return null;
		}
		return res.value();
	}

	private void exchangeOf(
			HestiaExchange e, Site site, Process process, FlowType defaultType
	) {
		double amount = e.value();
		if (amount == 0)
			return;
		var f = flows.get(e.term(), site, defaultType);
		if (f.isEmpty())
			return;

		var ex = process.output(f.flow(), amount);
		ex.unit = f.unit();

		switch (e) {
			case Input ignored -> ex.isInput = true;
			case Product product -> {
				if (product.isPrimary()) {
					process.quantitativeReference = ex;
				}
			}
			case Emission emission ->
					ex.description = emission.methodModelDescription();
			case Practice ignored -> {}
		}
	}

	private void mapDates(Cycle cycle, Process process) {
		var created = cycle.createdAt();
		if (created != null) {
			process.documentation.creationDate = created;
		}
		var updated = cycle.updatedAt();
		if (updated != null) {
			process.lastChange = updated.getTime();
		}
		var start = cycle.startDate();
		if (start != null) {
			process.documentation.validFrom = start;
		}
		var end = cycle.endDate();
		if (end != null) {
			process.documentation.validUntil = end;
		}
	}
}
