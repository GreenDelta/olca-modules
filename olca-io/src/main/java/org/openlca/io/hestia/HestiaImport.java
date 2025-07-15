package org.openlca.io.hestia;

import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.io.hestia.HestiaExchange.Emission;
import org.openlca.io.hestia.HestiaExchange.Input;
import org.openlca.io.hestia.HestiaExchange.Product;
import org.openlca.util.KeyGen;
import org.openlca.util.Res;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HestiaImport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HestiaClient client;
	private final IDatabase db;
	private final FlowMap flows;
	private final LocationMap locations;

	public HestiaImport(HestiaClient client, IDatabase db) {
		this.client = Objects.requireNonNull(client);
		this.db = Objects.requireNonNull(db);
		this.flows = FlowMap.create(db);
		this.locations = LocationMap.of(db);
	}

	public Res<Process> importCycle(String cycleId) {
		if (Strings.nullOrEmpty(cycleId))
			return Res.error("cycle ID must not be null or empty");

		var refId = KeyGen.get("cycle", cycleId);
		if (db.get(Process.class, refId) != null)
			return Res.error("a mapped process for cycle ID " + cycleId
				+ " already exists: " + refId);

		// fetch the cycle
		log.info("importing cycle {}", cycleId);
		var res = client.getCycle(cycleId);
		if (res.hasError())
			return res.wrapError("failed to fetch cycle " + cycleId);
		var cycle = res.value();

		// fetch the site
		var site = siteOf(cycle);

		// create and map the process
		try {
			var process = new Process();
			process.refId = refId;
			process.name = cycle.name();
			process.description = cycle.description();
			process.documentation = new ProcessDoc();
			process.location = locations.get(site);
			mapDates(cycle, process);

			for (var product : cycle.products()) {
				exchangeOf(product, site, process);
			}
			for (var input : cycle.inputs()) {
				exchangeOf(input, site, process);
			}
			for (var emission : cycle.emissions()) {
				exchangeOf(emission, site, process);
			}

			db.insert(process);
			return Res.of(process);
		} catch (Exception e) {
			return Res.error("mapping process data failed", e);
		}
	}

	private Site siteOf(Cycle cycle) {
		var site = cycle.site();
		if (site != null && Strings.notEmpty(site.id())) {
			var siteRes = client.getSite(site.id());
			if (!siteRes.hasError())
				return siteRes.value();
		}
		return site;
	}

	private void exchangeOf(HestiaExchange e, Site site, Process process) {
		double amount = e.value();
		if (amount == 0)
			return;
		var res = flows.get(e.term(), site);
		if (res.hasError()) {
			log.warn("could not map/create flow {}", res.error());
			return;
		}

		var f = res.value();
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
