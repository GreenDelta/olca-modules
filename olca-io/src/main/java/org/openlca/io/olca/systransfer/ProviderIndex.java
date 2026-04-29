package org.openlca.io.olca.systransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;

public class ProviderIndex {

	private final Map<String, List<ProviderInfo>> index;

	private ProviderIndex(List<ProviderInfo> candidates) {
		index = new HashMap<>();
		for (var c : candidates) {
			var flowId = c.flowId();
			if (flowId == null) continue;
			index.computeIfAbsent(flowId, k -> new ArrayList<>()).add(c);
		}
	}

	public static ProviderIndex of(IDatabase db) {
		return of(db, null);
	}

	public static ProviderIndex of(IDatabase db, ProductSystem system) {
		var candidates = system != null
			? ProviderInfo.allOf(db, system)
			: ProviderInfo.allOf(db);
		return new ProviderIndex(candidates);
	}

	public boolean isEmpty() {
		return index.isEmpty();
	}

	public List<ProviderInfo> allOf(String flowId) {
		return index.getOrDefault(flowId, List.of());
	}

}
