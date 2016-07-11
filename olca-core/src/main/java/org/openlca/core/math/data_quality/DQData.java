package org.openlca.core.math.data_quality;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.DQSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DQData {

	private static Logger log = LoggerFactory.getLogger(DQData.class);
	DQSystem processSystem;
	DQSystem exchangeSystem;
	Map<Long, int[]> processData = new HashMap<>();
	Map<LongPair, int[]> exchangeData = new HashMap<>();

	public static DQData load(IDatabase db, long productSystemId) {
		DQData data = new DQData();
		data.loadSystems(db, productSystemId);
		if (data.processSystem != null) {
			data.loadProcessEntries(db, productSystemId);
		}
		if (data.exchangeSystem != null) {
			data.loadExchangeEntries(db, productSystemId);
		}
		return data;
	}

	private DQData() {
		// hide constructor
	}

	private void loadSystems(IDatabase db, long productSystemId) {
		loadProcessSystem(db, productSystemId);
		loadExchangeSystem(db, productSystemId);
	}

	private void loadProcessSystem(IDatabase db, long productSystemId) {
		String query = getLoadSystemQuery("f_dq_system", productSystemId);
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long systemId = res.getLong("f_dq_system");
				if (processSystem == null) {
					processSystem = loadSystemFromDb(db, systemId);
				} else if (processSystem.getId() != systemId) {
					processSystem = null;
					return false;
				}
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading linked data quality systems", e);
		}
	}

	private void loadExchangeSystem(IDatabase db, long productSystemId) {
		String query = getLoadSystemQuery("f_exchange_dq_system", productSystemId);
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long systemId = res.getLong("f_exchange_dq_system");
				if (exchangeSystem == null) {
					exchangeSystem = loadSystemFromDb(db, systemId);
				} else if (exchangeSystem.getId() != systemId) {
					exchangeSystem = null;
					return false;
				}
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading linked data quality systems", e);
		}
	}

	private String getLoadSystemQuery(String field, long productSystemId) {
		String query = "SELECT DISTINCT " + field + " FROM tbl_processes";
		query += " INNER JOIN tbl_product_system_processes ON tbl_processes.id = tbl_product_system_processes.f_process ";
		query += " WHERE tbl_product_system_processes.f_product_system = " + productSystemId;
		return query;
	}

	private void loadProcessEntries(IDatabase db, long productSystemId) {
		String query = "SELECT id, dq_entry FROM tbl_processes";
		query += " INNER JOIN tbl_product_system_processes ON tbl_processes.id = tbl_product_system_processes.f_process ";
		query += " WHERE tbl_product_system_processes.f_product_system = " + productSystemId;
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long processId = res.getLong("id");
				String dqEntry = res.getString("dq_entry");
				processData.put(processId, processSystem.toValues(dqEntry));
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading process data quality entries", e);
		}
	}

	private void loadExchangeEntries(IDatabase db, long productSystemId) {
		String query = "SELECT f_owner, f_flow, dq_entry FROM tbl_exchanges";
		query += " INNER JOIN tbl_product_system_processes ON tbl_exchanges.f_owner = tbl_product_system_processes.f_process ";
		query += " WHERE tbl_product_system_processes.f_product_system = " + productSystemId;
		try {
			NativeSql.on(db).query(query.toString(), (res) -> {
				long processId = res.getLong("f_owner");
				long flowId = res.getLong("f_flow");
				String dqEntry = res.getString("dq_entry");
				exchangeData.put(new LongPair(processId, flowId), exchangeSystem.toValues(dqEntry));
				return true;
			});
		} catch (SQLException e) {
			log.error("Error loading process data quality entries", e);
		}
	}

	private DQSystem loadSystemFromDb(IDatabase db, long id) {
		if (id == 0l)
			return null;
		return new DQSystemDao(db).getForId(id);
	}

}
