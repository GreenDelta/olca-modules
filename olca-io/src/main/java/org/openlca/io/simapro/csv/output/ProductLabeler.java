package org.openlca.io.simapro.csv.output;


import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductLabeler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final TLongHashSet exportIds;
	private boolean withLongNames;
	private ProcessTable processes;

	private ProductLabeler(IDatabase db, TLongHashSet exportIds) {
		this.db = db;
		this.exportIds = exportIds;
	}

	static ProductLabeler of(SimaProExport export) {
		var exportIds = new TLongHashSet(export.processes.size(), 0.8f, 0L);
		for (var d : export.processes) {
			exportIds.add(d.id);
		}
		return new ProductLabeler(export.db, exportIds);
	}

	ProductLabeler withLongNames(boolean b) {
		this.withLongNames = b;
		return this;
	}

	String labelOf(Flow product, Process process) {
		if (product == null || product.name == null)
			return "?";
		var flowName = product.name.trim();
		String processName;
		if (process != null) {
			processName = process.name;
		} else {
			// try to find a default provider if required
			if (processTable == null) {
				processTable = ProcessTable.create(db);
			}
			var providers = processTable.getProviders(product.id);
			if (providers.isEmpty()) {
				log.warn("no providers found for flow {}", flowName);
				return flowName;
			}
			if (providers.size() > 1) {
				log.warn("multiple providers found for flow {}", flowName);
			}
			processName = providers.get(0).provider().name;
		}

		if (Strings.nullOrEmpty(processName)
				|| processName.startsWith("Dummy: "))
			return flowName;
		processName = processName.trim();
		return processName.equalsIgnoreCase(flowName)
				? flowName
				: flowName + " - " + processName;
	}

	String labelOfInput(Exchange e) {
		if (e == null || e.flow == null)
			return "?";
		var provider = providerOf(e);
		if (provider == null) {
			log.warn("no provider found for flow {}", e.flow.refId);
			return e.flow.name;
		}
		if (!exportIds.contains(provider.id)) {
			log.warn(
					"provider process {} not exported; default to dummy",
					provider.refId);
			return  e.flow.name;
		}
		return labelOf(e.flow, provider);
	}

	private Process providerOf(Exchange e) {
		if (e.defaultProviderId > 0) {
			var p = db.get(Process.class, e.defaultProviderId);
			if (p != null)
				return p;
			log.warn(
					"default provider {} does not exist; try default",
					e.defaultProviderId);
		}
		if (processes == null) {
			processes = ProcessTable.create(db);
		}
		var providers = processes.getProviders(e.flow.id);
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

}
