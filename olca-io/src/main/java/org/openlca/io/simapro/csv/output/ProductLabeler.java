package org.openlca.io.simapro.csv.output;

import java.util.Collection;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Strings;

class ProductLabeler {

	private final IDatabase db;
	private final TLongHashSet exportIds;
	private boolean withLongNames;
	private ProcessTable _processes;

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

	private String label(Flow product, Process process) {
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

}
