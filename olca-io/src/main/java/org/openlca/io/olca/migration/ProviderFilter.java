package org.openlca.io.olca.migration;

import java.util.List;

import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;

import gnu.trove.set.hash.TLongHashSet;

record ProviderFilter(
	TLongHashSet processes,
	TLongHashSet results,
	TLongHashSet subSystems
) {

	static ProviderFilter of(List<ProductSystem> systems) {
		if (systems == null || systems.isEmpty())
			return new ProviderFilter(null, null, null);

		var processes = new TLongHashSet();
		for (var system : systems) {
			if (system.referenceProcess != null) {
				processes.add(system.referenceProcess.id);
			}
		}

		TLongHashSet results = null;
		TLongHashSet subSystems = null;
		for (var system : systems) {
			for (var link : system.processLinks) {
				switch (link.providerType) {
					case ProviderType.RESULT -> {
						if (results == null) {
							results = new TLongHashSet();
						}
						results.add(link.providerId);
					}
					case ProviderType.SUB_SYSTEM -> {
						if (subSystems == null) {
							subSystems = new TLongHashSet();
						}
						subSystems.add(link.providerId);
					}
					default -> processes.add(link.providerId);
				}
			}
		}
		return new ProviderFilter(processes, results, subSystems);
	}

	boolean containsProcess(long id) {
		return processes != null && processes.contains(id);
	}

	boolean containsResult(long id) {
		return results != null && results.contains(id);
	}

	boolean containsSubSystem(long id) {
		return subSystems != null && subSystems.contains(id);
	}

	boolean hasProcesses() {
		return processes != null && !processes.isEmpty();
	}

	boolean hasResults() {
		return results != null && !results.isEmpty();
	}

	boolean hasSubSystems() {
		return subSystems != null && !subSystems.isEmpty();
	}
}
