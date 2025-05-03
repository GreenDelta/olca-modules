package org.openlca.io.olca;

import gnu.trove.map.hash.TLongDoubleHashMap;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.util.RefIdMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the process IDs and IDs of the product system links.
 */
class ProductSystemLinks {

	private final Config conf;
	private final ProductSystem system;
	private final RefIdMap<Long, String> srcIdMap;
	private final RefIdMap<String, Long> destIdMap;

	static void map(Config conf, ProductSystem system) {
		if (system == null)
			return;
		new ProductSystemLinks(conf, system).map();
	}

	private ProductSystemLinks(Config conf, ProductSystem system) {
		this.conf = conf;
		this.system = system;
		srcIdMap = RefIdMap.internalToRef(
				conf.source(), Process.class, Flow.class, Unit.class);
		destIdMap = RefIdMap.refToInternal(
				conf.target(), Process.class, Flow.class, Unit.class);
	}

	private long destId(Class<?> type, long sourceId) {
		if (sourceId == 0)
			return 0;
		String refId = srcIdMap.get(type, sourceId);
		if (refId == null)
			return 0;
		Long destId = destIdMap.get(type, refId);
		return destId == null ? 0 : destId;
	}

	private void map() {
		mapProcessIds();
		for (var link : system.processLinks) {
			link.providerId = destId(Process.class, link.providerId);
			link.processId = destId(Process.class, link.processId);
			link.flowId = destId(Flow.class, link.flowId);
			Ex ex = new Ex(link.exchangeId);
			link.exchangeId = ex.map().id;
		}
	}

	private void mapProcessIds() {
		List<Long> destProcessIds = new ArrayList<>();
		for (Long id : system.processes) {
			destProcessIds.add(destId(Process.class, id));
		}
		system.processes.clear();
		system.processes.addAll(destProcessIds);
	}

	private class Ex {

		long id;

		long processId;
		long flowId;
		long unitId;
		int isInput;
		double amount;
		long providerId;

		Ex(long id) {
			init(id);
		}

		void init(long id) {
			this.id = id;
			String sql = "SELECT f_owner, f_flow, f_unit, is_input, "
					+ "resulting_amount_value, f_default_provider "
					+ "from tbl_exchanges where id=" + id;
			try {
				NativeSql.on(conf.source()).query(sql, r -> {
					processId = r.getLong(1);
					flowId = r.getLong(2);
					unitId = r.getLong(3);
					isInput = r.getBoolean(4) ? 1 : 0;
					amount = r.getDouble(5);
					providerId = r.getLong(6);
					return false;
				});
			} catch (Exception e) {
				conf.log().error("failed to query exchange: " + sql, e);
			}
		}

		Ex map() {
			processId = destId(Process.class, processId);
			flowId = destId(Flow.class, flowId);
			unitId = destId(Unit.class, unitId);
			providerId = destId(Process.class, providerId);
			findMatch();
			return this;
		}

		void findMatch() {
			id = 0;
			String sql = "select id, resulting_amount_value from tbl_exchanges "
					+ "where f_owner=" + processId + " and f_flow=" + flowId
					+ " and f_unit=" + unitId + " and f_default_provider="
					+ providerId + " and is_input=" + isInput;
			try {
				id = queryId(sql);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to search exchange: {}", sql, e);
				id = -1;
			}
		}

		long queryId(String sql) {
			var vals = new TLongDoubleHashMap();
			NativeSql.on(conf.target()).query(sql, r -> {
				vals.put(r.getLong(1), r.getDouble(2));
				return true;
			});
			long exchangeId = -1;
			double delta = 0;
			for (long id : vals.keys()) {
				double dist = Math.abs(amount - vals.get(id));
				if (exchangeId == -1 || dist < delta) {
					exchangeId = id;
					delta = dist;
				}
			}
			return exchangeId;
		}
	}
}
