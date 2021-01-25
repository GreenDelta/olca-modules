package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectLongHashMap;

/** Upgrades the database to version 5. */
class Upgrade5 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 4 };
	}

	@Override
	public int getEndVersion() {
		return 5;
	}

	@Override
	public void exec(IDatabase db) {
		DbUtil u = new DbUtil(db);
		u.renameColumn("tbl_sources", "doi", "url VARCHAR(255)");
		u.renameColumn("tbl_process_links", "f_recipient", "f_process BIGINT");
		u.createColumn("tbl_process_links", "f_exchange BIGINT");
		updateLinks(db);
	}

	private void updateLinks(IDatabase db) {
		TObjectLongHashMap<LongPair> idx = inputIdx(db);
		try (Connection con = db.createConnection()) {
			con.setAutoCommit(false);
			Statement query = con.createStatement();
			query.setCursorName("UPDATE_LINKS");
			ResultSet cursor = query.executeQuery(
					"SELECT f_process, f_flow FROM tbl_process_links "
							+ "FOR UPDATE of f_exchange");
			PreparedStatement update = con.prepareStatement(
					"UPDATE tbl_process_links SET f_exchange = ? " +
							"WHERE CURRENT OF UPDATE_LINKS");
			while (cursor.next()) {
				long processId = cursor.getLong(1);
				long flowId = cursor.getLong(2);
				long exchangeId = idx.get(LongPair.of(processId, flowId));
				if (exchangeId > 0) {
					update.setLong(1, exchangeId);
					update.executeUpdate();
				}
			}
			cursor.close();
			query.close();
			update.close();
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException("failed to upgrade process links", e);
		}
	}

	private TObjectLongHashMap<LongPair> inputIdx(IDatabase db) {
		TObjectLongHashMap<LongPair> idx = new TObjectLongHashMap<>(
				Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
		Map<Long, FlowType> flowTypes = new HashMap<>();
		try {
			String sql = "SELECT id, flow_type from tbl_flows";
			NativeSql.on(db).query(sql, r -> {
				flowTypes.put(r.getLong(1), FlowType.valueOf(r.getString(2)));
				return true;
			});
			sql = "SELECT id, f_owner, f_flow, is_input from tbl_exchanges";
			NativeSql.on(db).query(sql, r -> {
				long exchangeId = r.getLong(1);
				long processId = r.getLong(2);
				long flowId = r.getLong(3);
				boolean isInput = r.getBoolean(4);
				if (!isInput || flowTypes.get(flowId) == FlowType.ELEMENTARY_FLOW)
					return true;
				idx.put(LongPair.of(processId, flowId), exchangeId);
				return true;
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to build exchange index");
		}
		return idx;
	}
}
