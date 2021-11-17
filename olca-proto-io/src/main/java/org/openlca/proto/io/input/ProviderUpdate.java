package org.openlca.proto.io.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Process;

class ProviderUpdate implements Runnable {

	private final IDatabase db;
	private final List<Link> links = new ArrayList<>();

	ProviderUpdate(IDatabase db) {
		this.db = db;
	}

	void add(Link link) {
		if (link == null)
			return;
		links.add(link);
	}

	@Override
	public void run() {
		if (links.isEmpty())
			return;

		// first transform the links into a fast map
		var processIds = new HashMap<String, Long>();
		db.allDescriptorsOf(Process.class)
			.forEach(d -> processIds.put(d.refId, d.id));
		var map = new TLongObjectHashMap<TIntLongHashMap>();
		for (var link : links) {
			if (link.exchangeId == 0)
				continue;
			var processID = processIds.get(link.processId);
			if (processID == null)
				continue;
			var providerID = processIds.get(link.providerId);
			if (providerID == null)
				continue;
			var providers = map.get(processID);
			if (providers == null) {
				providers = new TIntLongHashMap();
				map.put(processID, providers);
			}
			providers.put(link.exchangeId, providerID);
		}

		// update the exchanges table in a single table scan
		var sql = "select f_owner, internal_id, " +
			"f_default_provider from tbl_exchanges";
		NativeSql.on(db).updateRows(sql, r -> {
			long processID = r.getLong(1);
			int exchangeID = r.getInt(2);
			var providers = map.get(processID);
			if (providers == null)
				return true;
			long provider = providers.get(exchangeID);
			if (provider == 0L)
				return true;
			r.updateLong(3, provider);
			r.updateRow();
			return true;
		});

		this.links.clear();
	}

	static class Link {

		private String processId;
		private int exchangeId;
		private String providerId;

		static Link forProcess(String refId) {
			var link = new Link();
			link.processId = refId;
			return link;
		}

		Link withExchangeId(int id) {
			this.exchangeId = id;
			return this;
		}

		Link withProvider(String refId) {
			this.providerId = refId;
			return this;
		}
	}

}
