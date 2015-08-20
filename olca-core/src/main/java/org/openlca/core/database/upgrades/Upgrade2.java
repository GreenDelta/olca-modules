package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.NativeSql.QueryResultHandler;
import org.openlca.core.model.Location;

public class Upgrade2 implements IUpgrade {

	private IDatabase database;
	private UpgradeUtil util;

	@Override
	public int[] getInitialVersions() {
		return new int[] { 3 };
	}

	@Override
	public int getEndVersion() {
		return 4;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		this.database = database;
		this.util = new UpgradeUtil(database);
		convertProcessKmzData();
		util.checkDropColumn("tbl_processes", "kmz");
		addVersionFields();
		addSocialTables();
	}

	/**
	 * In the new version there is no process specific KML anymore. We convert
	 * existing KML data to own locations.
	 */
	private void convertProcessKmzData() throws SQLException {
		try (Connection con = database.createConnection()) {
			String updateSql = "UPDATE tbl_processes SET f_location = ? WHERE id = ?";
			PreparedStatement stmt = con.prepareStatement(updateSql);
			String query = "SELECT id, ref_id, name, kmz FROM tbl_processes WHERE kmz is not null";
			NativeSql.on(database).query(query, new KmzResultHandler(stmt));
			stmt.close();
			con.commit();
		}
	}

	/**
	 * The fields version and last_change moved to the RootEntity class. Also
	 * parameters are now root entities.
	 */
	private void addVersionFields() throws Exception {
		String[] tables = { "tbl_categories", "tbl_impact_categories",
				"tbl_locations", "tbl_nw_sets", "tbl_parameters", "tbl_units" };
		for (String table : tables) {
			util.checkCreateColumn(table, "version", "version BIGINT");
			util.checkCreateColumn(table, "last_change", "last_change BIGINT");
		}
		util.checkCreateColumn("tbl_parameters", "ref_id",
				"ref_id VARCHAR(36)");
		List<String> updates = new ArrayList<>();
		NativeSql.on(database).query("select id from tbl_parameters", (r) -> {
			long id = r.getLong(1);
			String update = "update tbl_parameters set ref_id = '"
					+ UUID.randomUUID().toString() + "' where id = " + id;
			updates.add(update);
			return true;
		});
		NativeSql.on(database).batchUpdate(updates);
	}

	private void addSocialTables() throws Exception {
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
		util.checkCreateTable("tbl_social_indicators", indicators);
		String aspects = "CREATE TABLE tbl_social_aspects ( "
				+ "id BIGINT NOT NULL, "
				+ "f_process BIGINT, "
				+ "f_indicator BIGINT, "
				+ "activity_value DOUBLE, "
				+ "raw_amount VARCHAR(255), "
				+ "comment CLOB(64 K), "
				+ "f_source BIGINT, "
				+ "quality VARCHAR(255), "
				+ "PRIMARY KEY (id)) ";
		util.checkCreateTable("tbl_social_aspects", aspects);

	}

	private class KmzResultHandler implements QueryResultHandler {

		private LocationDao locationDao = new LocationDao(database);
		private PreparedStatement processUpdateStatement;

		private KmzResultHandler(PreparedStatement processUpdateStatement) {
			this.processUpdateStatement = processUpdateStatement;
		}

		@Override
		public boolean nextResult(ResultSet result) throws SQLException {
			long id = result.getLong("id");
			String name = result.getString("name");
			String refId = result.getString("ref_id");
			byte[] kmz = result.getBytes("kmz");
			long locationId = insertLocation(createName(name),
					createDescription(name, refId), kmz);
			updateProcess(id, locationId);
			return true;
		}

		private long insertLocation(String name, String description,
				byte[] kmz) {
			Location location = new Location();
			location.setName(name);
			location.setDescription(description);
			location.setRefId(UUID.randomUUID().toString());
			location.setKmz(kmz);
			return locationDao.insert(location).getId();
		}

		private void updateProcess(long processId, long locationId)
				throws SQLException {
			processUpdateStatement.setLong(1, locationId);
			processUpdateStatement.setLong(2, processId);
			processUpdateStatement.executeUpdate();
		}

		private String createName(String name) {
			String locationName = "Location of process " + name;
			if (locationName.length() > 255)
				locationName = locationName.substring(0, 255);
			return locationName;
		}

		private String createDescription(String name, String refId) {
			return "Location was specified in process " + name + " (" + refId
					+ ")";
		}
	}

}
