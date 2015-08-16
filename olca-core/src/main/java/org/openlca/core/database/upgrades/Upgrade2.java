package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		dropKmzColumn();
	}

	private void convertProcessKmzData() throws SQLException {
		try (Connection con = database.createConnection()) {
			PreparedStatement processUpdateStatement = con
					.prepareStatement("UPDATE tbl_processes SET f_location = ? WHERE id = ?");
			NativeSql
					.on(database)
					.query("SELECT id, ref_id, name, kmz FROM tbl_processes WHERE kmz is not null",
							new KmzResultHandler(processUpdateStatement));
			con.commit();
			processUpdateStatement.close();
		}
	}

	private void dropKmzColumn() throws Exception {
		util.checkDropColumn("tbl_processes", "kmz");
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

		private long insertLocation(String name, String description, byte[] kmz) {
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
