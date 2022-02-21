package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.openlca.core.database.IDatabase;

class Upgrade08 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 7 };
	}

	@Override
	public int getEndVersion() {
		return 8;
	}

	@Override
	public void exec(IDatabase db) {
		DbUtil u = new DbUtil(db);

		u.createColumn("tbl_process_links", "is_system_link SMALLINT default 0");
		u.createColumn("tbl_impact_methods", "f_author BIGINT");
		u.createColumn("tbl_impact_methods", "f_generator BIGINT");
		u.createColumn("tbl_process_docs", "preceding_dataset VARCHAR(255)");
		u.createColumn("tbl_project_variants", "is_disabled SMALLINT default 0");

		// move the tbl_process_sources to a new table tbl_source_links
		// we also want to use this table for LCIA methods (etc.) in the future
		if (u.tableExists("tbl_source_links")) {
			// avoid the update statements below
			return;
		}

		u.createTable("tbl_source_links",
				"CREATE TABLE tbl_source_links ( "
						+ "f_owner  BIGINT, "
						+ "f_source BIGINT)");

		if (!u.tableExists("tbl_process_sources")) {
			// nothing to migrate
			return;
		}

		String query = "SELECT f_process_doc, f_source"
				+ " FROM tbl_process_sources";
		String insert = "INSERT INTO tbl_source_links"
				+ " (f_owner, f_source) VALUES (?, ?)";

		try (Connection con = db.createConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				PreparedStatement batch = con.prepareStatement(insert)) {
			while (rs.next()) {
				long doc = rs.getLong(1);
				long source = rs.getLong(2);
				batch.setLong(1, doc);
				batch.setLong(2, source);
				batch.addBatch();
			}
			batch.executeBatch();
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to copy process sources to tbl_source_links", e);
		}
	}
}
