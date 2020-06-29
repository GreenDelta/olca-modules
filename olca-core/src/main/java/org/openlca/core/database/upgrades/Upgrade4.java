package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.NativeSql.QueryResultHandler;

class Upgrade4 implements IUpgrade {

	private IDatabase database;
	private DbUtil util;

	@Override
	public int[] getInitialVersions() {
		return new int[] { 3 };
	}

	@Override
	public int getEndVersion() {
		return 4;
	}

	@Override
	public void exec(IDatabase database) {
		this.database = database;
		this.util = new DbUtil(database);
		if (util.columnExists("tbl_processes", "kmz")) {
			convertProcessKmzData();
			util.dropColumn("tbl_processes", "kmz");
		}
		addVersionFields();
		createSocialTables();
		createCurrencyTable();
		createCostColumns();
		util.createColumn("tbl_locations", "f_category BIGINT");
		util.createColumn("tbl_parameters", "f_category BIGINT");
		util.renameColumn("tbl_categories", "f_parent_category",
				"f_category BIGINT");
		util.createColumn("tbl_exchanges", "description " + util.getTextType());
		util.createColumn("tbl_flows", "synonyms VARCHAR(32672)");
		Upgrade4Files.apply(database);
	}

	/**
	 * In the new version there is no process specific KML anymore. We convert
	 * existing KML data to own locations.
	 */
	private void convertProcessKmzData() {
		String updateSql = "UPDATE tbl_processes SET f_location = ? WHERE id = ?";
		String insertSql = "INSERT INTO tbl_locations(id, name, description, ref_id, kmz) "
				+ "VALUES (?, ?, ?, ?, ?)";
		try (Connection con = database.createConnection();
				PreparedStatement updateStmt = con.prepareStatement(updateSql);
				PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
			KmzResultHandler handler = new KmzResultHandler(
					updateStmt, insertStmt);
			handler.currentId = getSequenceId(con);
			String query = "SELECT id, ref_id, name, kmz "
					+ "FROM tbl_processes " + "WHERE kmz is not null";
			NativeSql.on(database).query(query, handler);
			updateSequenceId(con, handler.currentId);
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to move process KML data to new locations", e);
		}
	}

	private long getSequenceId(Connection con) throws SQLException {
		String query = "SELECT SEQ_COUNT " + "FROM SEQUENCE "
				+ "WHERE SEQ_NAME = 'entity_seq'";
		try (Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			rs.next();
			return rs.getLong("SEQ_COUNT");
		}
	}

	private void updateSequenceId(Connection con, long newId)
			throws SQLException {
		String query = "UPDATE SEQUENCE " + "SET SEQ_COUNT = " + newId
				+ " WHERE SEQ_NAME = 'entity_seq'";
		try (Statement stmt = con.createStatement()) {
			stmt.executeUpdate(query);
		}
	}

	/**
	 * The fields version and last_change moved to the RootEntity class. Also
	 * parameters are now root entities.
	 */
	private void addVersionFields() {
		String[] tables = { "tbl_categories", "tbl_impact_categories",
				"tbl_locations", "tbl_nw_sets", "tbl_parameters", "tbl_units" };
		for (String table : tables) {
			util.createColumn(table, "version BIGINT");
			util.createColumn(table, "last_change BIGINT");
		}

		// set reference IDs on parameters
		util.createColumn("tbl_parameters", "ref_id VARCHAR(36)");
		List<String> updates = new ArrayList<>();
		try {
			NativeSql.on(database).query("select id from tbl_parameters", (r) -> {
				long id = r.getLong(1);
				String update = "update tbl_parameters set ref_id = '"
						+ UUID.randomUUID().toString() + "' where id = "
						+ id;
				updates.add(update);
				return true;
			});
			NativeSql.on(database).batchUpdate(updates);
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to set reference IDs in parameters", e);
		}
	}

	private void createCurrencyTable() {
		util.createTable("tbl_currencies",
				"CREATE TABLE tbl_currencies ( "
						+ "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), "
						+ "ref_id VARCHAR(36), "
						+ "version BIGINT, "
						+ "last_change BIGINT, "
						+ "f_category BIGINT, "
						+ "description CLOB(64 K), "
						+ "code VARCHAR(255), "
						+ "conversion_factor DOUBLE, "
						+ "f_reference_currency BIGINT, "
						+ "PRIMARY KEY (id)) ");
	}

	private void createSocialTables() {
		String indicators = "CREATE TABLE tbl_social_indicators ( "
				+ "id BIGINT NOT NULL, "
				+ "ref_id VARCHAR(36), "
				+ "name VARCHAR(255), "
				+ "version BIGINT, "
				+ "last_change BIGINT, "
				+ "f_category BIGINT, "
				+ "description CLOB(64 K), "
				+ "activity_variable VARCHAR(255), "
				+ "f_activity_quantity BIGINT, "
				+ "f_activity_unit BIGINT, "
				+ "unit_of_measurement VARCHAR(255), "
				+ "evaluation_scheme CLOB(64 K), "
				+ "PRIMARY KEY (id)) ";
		util.createTable("tbl_social_indicators", indicators);
		String aspects = "CREATE TABLE tbl_social_aspects ( "
				+ "id BIGINT NOT NULL, "
				+ "f_process BIGINT, "
				+ "f_indicator BIGINT, "
				+ "activity_value DOUBLE, "
				+ "raw_amount VARCHAR(255), "
				+ "risk_level VARCHAR(255), "
				+ "comment CLOB(64 K), "
				+ "f_source BIGINT, "
				+ "quality VARCHAR(255), "
				+ "PRIMARY KEY (id)) ";
		util.createTable("tbl_social_aspects", aspects);
	}

	private void createCostColumns() {
		util.createColumn("tbl_processes", "f_currency BIGINT");
		util.createColumn("tbl_exchanges", "cost_value DOUBLE");
		util.createColumn("tbl_exchanges", "cost_formula VARCHAR(1000)");
		util.createColumn("tbl_exchanges", "f_currency BIGINT");
	}

	private static class KmzResultHandler implements QueryResultHandler {

		private final PreparedStatement processUpdateStatement;
		private final PreparedStatement locationInsertStatement;
		private long currentId;

		private KmzResultHandler(PreparedStatement processUpdateStatement,
				PreparedStatement locationInsertStatement) {
			this.processUpdateStatement = processUpdateStatement;
			this.locationInsertStatement = locationInsertStatement;
		}

		@Override
		public boolean accept(ResultSet r) throws SQLException {
			long id = r.getLong("id");
			String name = r.getString("name");
			String refId = r.getString("ref_id");
			byte[] kmz = r.getBytes("kmz");
			String description = "Location was specified in process "
					+ name + " (" + refId + ")";
			String locName = "Location of process " + name;
			if (locName.length() > 255)
				locName = locName.substring(0, 255);
			long locationId = insertLocation(locName, description, kmz);
			updateProcess(id, locationId);
			return true;
		}

		private long insertLocation(String name, String description, byte[] kmz)
				throws SQLException {
			locationInsertStatement.setLong(1, ++currentId);
			locationInsertStatement.setString(2, name);
			locationInsertStatement.setString(3, description);
			locationInsertStatement.setString(4, UUID.randomUUID().toString());
			locationInsertStatement.setBytes(5, kmz);
			locationInsertStatement.executeUpdate();
			return currentId;
		}

		private void updateProcess(long processId, long locationId)
				throws SQLException {
			processUpdateStatement.setLong(1, locationId);
			processUpdateStatement.setLong(2, processId);
			processUpdateStatement.executeUpdate();
		}
	}
}
