package org.openlca.core.math.data_quality;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DQData {

	private static Logger log = LoggerFactory.getLogger(DQData.class);
	Map<Long, double[]> processData = new HashMap<>();
	Map<LongPair, double[]> exchangeData = new HashMap<>();

	public static DQData load(IDatabase db, DQCalculationSetup setup) {
		DQData data = new DQData();
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
				processData.put(processId, toDouble(setup.processDqSystem.toValues(dqEntry)));
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading process data quality entries", e);
		}
	}

	private void loadExchangeEntries(IDatabase db, DQCalculationSetup setup) {
		String query = "SELECT f_owner, f_flow, dq_entry FROM tbl_exchanges";
		query += " INNER JOIN tbl_product_system_processes ON tbl_exchanges.f_owner = tbl_product_system_processes.f_process ";
		query += " WHERE tbl_product_system_processes.f_product_system = " + setup.productSystemId;
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long processId = res.getLong("f_owner");
				long flowId = res.getLong("f_flow");
				String dqEntry = res.getString("dq_entry");
				exchangeData.put(new LongPair(processId, flowId), toDouble(setup.exchangeDqSystem.toValues(dqEntry)));
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading process data quality entries", e);
		}
	}

	private double[] toDouble(int[] values) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i];
		}
		return result;
	}

}
