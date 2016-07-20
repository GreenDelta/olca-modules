package org.openlca.core.math.data_quality;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DQData {

	private static Logger log = LoggerFactory.getLogger(DQData.class);
	Map<Long, double[]> processData = new HashMap<>();
	Map<LongPair, double[]> exchangeData = new HashMap<>();
	DQStatistics statistics = new DQStatistics();
	private Set<Long> relevantFlowIds = new HashSet<>();

	public static DQData load(IDatabase db, DQCalculationSetup setup, long[] relevantFlowIds) {
		DQData data = new DQData();
		for (long id : relevantFlowIds) {
			data.relevantFlowIds.add(id);
		}
		if (setup.processDqSystem != null) {
			data.loadProcessEntries(db, setup);
		}
		if (setup.exchangeDqSystem != null) {
			data.loadExchangeEntries(db, setup);
		}
		return data;
	}

	private DQData() {
		// hide constructor
	}

	private void loadProcessEntries(IDatabase db, DQCalculationSetup setup) {
		String query = "SELECT id, dq_entry FROM tbl_processes";
		query += " INNER JOIN tbl_product_system_processes ON tbl_processes.id = tbl_product_system_processes.f_process ";
		query += " WHERE tbl_product_system_processes.f_product_system = " + setup.productSystemId;
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long processId = res.getLong("id");
				String dqEntry = res.getString("dq_entry");
				int[] values = setup.processDqSystem.toValues(dqEntry);
				processData.put(processId, toDouble(values));
				increaseCounter(statistics.processCounts, 0);
				for (int i = 0; i < values.length; i++) {
					if (values[i] == 0)
						continue;
					increaseCounter(statistics.processCounts, i + 1);
				}
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading process data quality entries", e);
		}
	}

	private <T> void increaseCounter(Map<T, Integer> map, T key) {
		if (!map.containsKey(key)) {
			map.put(key, 1);
			return;
		}
		map.put(key, map.get(key) + 1);
	}

	private void loadExchangeEntries(IDatabase db, DQCalculationSetup setup) {
		String query = "SELECT f_owner, f_flow, dq_entry FROM tbl_exchanges";
		query += " INNER JOIN tbl_product_system_processes ON tbl_exchanges.f_owner = tbl_product_system_processes.f_process ";
		query += " WHERE tbl_product_system_processes.f_product_system = " + setup.productSystemId;
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long processId = res.getLong("f_owner");
				long flowId = res.getLong("f_flow");
				if (!relevantFlowIds.contains(flowId))
					return true;
				String dqEntry = res.getString("dq_entry");
				int[] values = setup.exchangeDqSystem.toValues(dqEntry);
				exchangeData.put(new LongPair(processId, flowId), toDouble(values));
				increaseCounter(getMap(statistics.exchangeCounts, 0l), 0);
				increaseCounter(getMap(statistics.exchangeCounts, processId), 0);
				for (int i = 0; i < values.length; i++) {
					if (values[i] == 0)
						continue;
					increaseCounter(getMap(statistics.exchangeCounts, 0l), i + 1);
					increaseCounter(getMap(statistics.exchangeCounts, processId), i + 1);
				}
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading process data quality entries", e);
		}
	}

	private Map<Integer, Integer> getMap(Map<Long, Map<Integer, Integer>> map, long id) {
		if (!map.containsKey(id)) {
			map.put(id, new HashMap<>());
		}
		return map.get(id);
	}

	private double[] toDouble(int[] values) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i];
		}
		return result;
	}

}
