package org.openlca.io.maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Flow;

public class FlowSync {

	private final IDatabase db;
	private final Map<String, FlowMapEntry> flowMap;
	private final Map<String, SyncFlow> cache;

	private ImportLog log;

	private FlowSync(IDatabase db, FlowMap flowMap) {
		this.db = Objects.requireNonNull(db);
		this.flowMap = flowMap == null
			? Collections.emptyMap()
			: flowMap.index();
		cache = new HashMap<>();
	}

	public FlowSync withLog(ImportLog log) {
		this.log = log;
		return this;
	}

	public static FlowSync of(IDatabase db, FlowMap flowMap) {
		return new FlowSync(db, flowMap);
	}

	public SyncFlow get(String key) {
		if (key == null)
			return SyncFlow.empty();

		var cached = cache.get(key);
		if (cached != null)
			return cached;

		// try to load a mapped flow first
		var entry = flowMap.get(key);
		if (entry != null && entry.targetFlow() != null) {
			var mapped = SyncFlow.mapped(entry, db);
			if (!mapped.isEmpty()) {
				cache.put(key, mapped);
				return mapped;
			}
			if (log != null) {
				log.warn("a mapping entry exists for flow '"
					+ key + "' but a matching target flow "
					+ entry.targetFlowId() + " could not be found");
			}
		}

		// try to load it directly from the database
		var flow = db.get(Flow.class, key);
		var synced = flow != null
			? SyncFlow.of(flow)
			: SyncFlow.empty();
		cache.put(key, synced);
		return synced;
	}

	public void put(Flow flow) {
		cache.put(flow.refId, SyncFlow.of(flow));
	}

	public void put(String key, SyncFlow flow) {
		cache.put(key, flow);
	}

	public SyncFlow createIfAbsent(String key, Supplier<Flow> fn) {
		var synced = get(key);
		if (!synced.isEmpty())
			return synced;
		var flow = fn.get();
		var syncFlow = SyncFlow.of(flow);
		cache.put(key, syncFlow);
		return syncFlow;
	}

}
