package org.openlca.io.olca.systransfer;

import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;

import gnu.trove.set.hash.TLongHashSet;

record ProviderFilter(
	TLongHashSet processes,
	TLongHashSet results,
	TLongHashSet systems
) {

	static ProviderFilter of(ProductSystem system) {
		if (system == null)
			return new ProviderFilter(null, null, null);
		TLongHashSet processes = null;
		if (system.referenceProcess != null) {
			processes = new TLongHashSet();
			processes.add(system.referenceProcess.id);
		}
		TLongHashSet results = null;
		TLongHashSet systems = null;
		for (var link : system.processLinks) {
			switch (link.providerType) {
				case ProviderType.RESULT -> {
					if (results == null) {
						results = new TLongHashSet();
					}
					results.add(link.providerId);
				}
				case ProviderType.SUB_SYSTEM -> {
					if (systems == null) {
						systems = new TLongHashSet();
					}
					systems.add(link.providerId);
				}
				default -> {
					if (processes == null) {
						processes = new TLongHashSet();
					}
					processes.add(link.providerId);
				}
			}
		}
		return new ProviderFilter(processes, results, systems);
	}

	boolean containsProcess(long id) {
		return processes != null && processes.contains(id);
	}

	boolean containsResult(long id) {
		return results != null && results.contains(id);
	}

	boolean containsSystem(long id) {
		return systems != null && systems.contains(id);
	}

	boolean hasProcesses() {
		return processes != null && !processes.isEmpty();
	}

	boolean hasResults() {
		return results != null && !results.isEmpty();
	}

	boolean hasSystems() {
		return systems != null && !systems.isEmpty();
	}
}
