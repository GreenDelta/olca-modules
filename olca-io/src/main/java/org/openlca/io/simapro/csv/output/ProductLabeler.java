package org.openlca.io.simapro.csv.output;


import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.set.hash.TLongHashSet;

class ProductLabeler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final SimaProExport config;
	private final TLongHashSet exportIds;
	private ProviderMap processes;

	private ProductLabeler(SimaProExport export) {
		this.config = export;
		exportIds = new TLongHashSet(export.processes.size(), 0.8f, 0L);
		for (var d : export.processes) {
			exportIds.add(d.id);
		}
	}

	static ProductLabeler of(SimaProExport export) {
		return new ProductLabeler(export);
	}

	String labelOf(Flow product, Process process) {
		if (product == null || product.name == null)
			return "?";
		if (process == null
				|| Strings.isBlank(process.name)
				|| process.name.startsWith("Dummy: "))
			return labelOf(product);

		var label = product.name.trim();

		// location
		if (config.withLocationSuffixes) {
			var loc = process.location != null
					? process.location
					: product.location;
			var locSuffix = suffixOf(loc);
			if (locSuffix != null) {
				label += locSuffix;
			}
		}

		if (config.withProcessSuffixes
			&& !Objects.equals(product.name, process.name)) {
			label += " | " + process.name;
		}

		if (config.withTypeSuffixes) {
			label += process.processType == ProcessType.LCI_RESULT
					? ", S"
					: ", U";
		}
		return label;
	}

	String labelOf(Flow product) {
		if (product == null || Strings.isBlank(product.name))
			return "?";
		if (config.withLocationSuffixes) {
			var locSuffix = suffixOf(product.location);
			return locSuffix != null
					? product.name + locSuffix
					: product.name;
		}
		return product.name;
	}

	String labelOfInput(Exchange e) {
		if (e == null || e.flow == null)
			return "?";
		var provider = providerOf(e);
		if (provider == null) {
			log.warn("no provider found for flow {}", e.flow.refId);
			return labelOf(e.flow);
		}
		if (!exportIds.contains(provider.id)) {
			log.warn(
					"provider process {} not exported; default to dummy",
					provider.refId);
			return labelOf(e.flow);
		}
		return labelOf(e.flow, provider);
	}

	private Process providerOf(Exchange e) {
		var db = config.db;
		if (e.defaultProviderId > 0) {
			var p = db.get(Process.class, e.defaultProviderId);
			if (p != null)
				return p;
			log.warn(
					"default provider {} does not exist; try default",
					e.defaultProviderId);
		}
		if (processes == null) {
			processes = ProviderMap.create(db);
		}
		var providers = processes.getProvidersOf(e.flow.id);
		if (providers.isEmpty())
			return null;
		Process candidate = null;
		for (var techFlow : providers) {
			var p = db.get(Process.class, techFlow.providerId());
			if (p != null) {
				if (exportIds.contains(p.id))
					return p;
				candidate = p;
			}
		}
		if (providers.size() > 1) {
			log.warn(
					"multiple possible providers available for flow {}",
					e.flow.refId);
		}
		return candidate;
	}

	private String suffixOf(Location loc) {
		return loc != null && Strings.isNotBlank(loc.code)
				? " {" + loc.code + "}"
				: null;
	}
}
