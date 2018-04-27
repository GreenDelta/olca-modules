package org.openlca.io.olca;

import gnu.trove.map.hash.TLongDoubleHashMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.util.RefIdMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the process IDs and IDs of the product system links.
 */
class ProductSystemLinks {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase sourceDb;
	private IDatabase destDb;
	private ProductSystem system;

	private RefIdMap<Long, String> srcIdMap;
	private RefIdMap<String, Long> destIdMap;

	public static void map(IDatabase sourceDb, IDatabase destDb,
			ProductSystem system) {
		if (sourceDb == null || destDb == null || system == null)
			return;
		new ProductSystemLinks(sourceDb, destDb, system).map();
	}

	private ProductSystemLinks(IDatabase sourceDb, IDatabase destDb,
			ProductSystem system) {
		this.sourceDb = sourceDb;
		this.destDb = destDb;
		this.system = system;
		srcIdMap = RefIdMap.internalToRef(sourceDb, Process.class, Flow.class,
				Unit.class);
		destIdMap = RefIdMap.refToInternal(destDb, Process.class, Flow.class,
				Unit.class);
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
		for (ProcessLink link : system.processLinks) {
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
				NativeSql.on(sourceDb).query(sql, r -> {
					processId = r.getLong(1);
					flowId = r.getLong(2);
					unitId = r.getLong(3);
					isInput = r.getBoolean(4) ? 1 : 0;
					amount = r.getDouble(5);
					providerId = r.getLong(6);
					return false;
				});
			} catch (Exception e) {
				log.error("failed to query exchange: " + sql, e);
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
				log.error("failed to search exchange: " + sql, e);
				id = -1;
			}
		}

		long queryId(String sql) throws SQLException {
			TLongDoubleHashMap vals = new TLongDoubleHashMap();
			NativeSql.on(destDb).query(sql, r -> {
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
