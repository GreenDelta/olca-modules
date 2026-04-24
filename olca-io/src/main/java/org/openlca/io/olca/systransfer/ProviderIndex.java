package org.openlca.io.olca.systransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;

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
		var candidates = ProviderInfo.allOf(db);
		return new ProviderIndex(candidates);
	}

	public int size() {
		return index.size();
	}

	public boolean isEmpty() {
		return index.isEmpty();
	}

	public List<ProviderInfo> allOf(String flowId) {
		return index.getOrDefault(flowId, List.of());
	}

}
