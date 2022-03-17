package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.openlca.core.database.IDatabase;

import gnu.trove.map.hash.TLongIntHashMap;

class Upgrade07 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 6 };
	}

	@Override
	public int getEndVersion() {
		return 7;
	}

	@Override
	public void exec(IDatabase db) {
		DbUtil u = new DbUtil(db);

		// add a new column for storing parameter aggregation
		// functions for regionalized LCIA methods
		u.createColumn("tbl_impact_methods", "parameter_mean VARCHAR(255)");

		// when the internal ID columns already exist make sure
		// to not overwrite them with the procedures below
		if (u.columnExists("tbl_processes", "last_internal_id")
				&& u.columnExists("tbl_exchanges", "internal_id"))
			return;

		// create data set internal IDs for exchanges
		u.createColumn("tbl_processes", "last_internal_id INTEGER");
		u.createColumn("tbl_exchanges", "internal_id INTEGER");

		TLongIntHashMap counter = new TLongIntHashMap();

		// set internal IDs in exchanges
		String query = "SELECT * FROM tbl_exchanges";
		try (Connection con = db.createConnection();
				Statement stmt = con.createStatement(
						ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				long pid = rs.getLong("f_owner");
				int last = counter.get(pid) + 1;
				counter.put(pid, last);
				rs.updateInt("internal_id", last);
				rs.updateRow();
			}
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to set internal IDs in exchanges", e);
		}

		// set last internal IDs in processes
		query = "SELECT * FROM tbl_processes";
		try (Connection con = db.createConnection();
				Statement stmt = con.createStatement(
						ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				long pid = rs.getLong("id");
				int last = counter.get(pid);
				rs.updateInt("last_internal_id", last);
				rs.updateRow();
			}
			con.commit();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to set last internal IDs in processes", e);
		}
	}
}
