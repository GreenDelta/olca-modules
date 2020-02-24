package org.openlca.core.database.upgrades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

class Upgrade9 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{8};
	}

	@Override
	public int getEndVersion() {
		return 9;
	}

	@Override
	public void exec(IDatabase db) {
		DbUtil u = new DbUtil(db);

		// make LCIA categories stand-alone entities
		u.createColumn("tbl_impact_categories", "f_category BIGINT");

		// support regionalization of exchanges and characterization factors
		u.createColumn("tbl_exchanges", "f_location BIGINT");
		u.createColumn("tbl_impact_factors", "f_location BIGINT");

		// new column for GeoJSON data
		u.createColumn("tbl_locations", "geodata BLOB(32 M)");

		if (u.tableExists("tbl_impact_links")) {
			// if the table tbl_impact_links already exists we assume
			// that the update was already executed
			return;
		}
		u.createTable("tbl_impact_links",
				"CREATE TABLE tbl_impact_links (" +
						" f_impact_method    BIGINT," +
						" f_impact_category  BIGINT)");
		try {
			NativeSql.on(db).runUpdate(
					"INSERT INTO tbl_impact_links "
							+ " (f_impact_method, f_impact_category) "
							+ " select f_impact_method, id "
							+ " from tbl_impact_categories");
		} catch (Exception e) {
			throw new RuntimeException("failed to copy impact links", e);
		}

		// parameters
		u.createColumn("tbl_impact_categories", "parameter_mean VARCHAR(255)");
		moveImpactParameters(db);
	}

	private void moveImpactParameters(IDatabase db) {
		AtomicLong nextID = new AtomicLong(DbUtil.getLastID(db) + 5L);
		try {
			// collect the LCIA method -> LCIA category relations
			HashMap<Long, List<Long>> methodImpacts = new HashMap<>();
			String linkQuery = "select f_impact_method, " +
					"f_impact_category from tbl_impact_links";
			NativeSql.on(db).query(linkQuery, r -> {
				long methodID = r.getLong(1);
				long impactID = r.getLong(2);
				List<Long> impacts = methodImpacts.computeIfAbsent(
						methodID, id -> new ArrayList<>());
				impacts.add(impactID);
				return true;
			});

			String columns =
					/* 1 */ "ID, " +
					/* 2 */ "REF_ID, " +
					/* 3 */ "NAME, " +
					/* 4 */ "DESCRIPTION, " +
					/* 5 */ "VERSION, " +
					/* 6 */ "LAST_CHANGE, " +
					/* 7 */ "F_CATEGORY, " +
					/* 8 */ "IS_INPUT_PARAM, " +
					/* 9 */ "F_OWNER, " +
					/* 10 */ "SCOPE, " +
					/* 11 */ "VALUE, " +
					/* 12 */ "FORMULA, " +
					/* 13 */ "EXTERNAL_SOURCE, " +
					/* 14 */ "SOURCE_TYPE, " +
					/* 15 */ "DISTRIBUTION_TYPE, " +
					/* 16 */ "PARAMETER1_VALUE, " +
					/* 17 */ "PARAMETER1_FORMULA, " +
					/* 18 */ "PARAMETER2_VALUE, " +
					/* 19 */ "PARAMETER2_FORMULA, " +
					/* 20 */ "PARAMETER3_VALUE, " +
					/* 21 */ "PARAMETER3_FORMULA ";

			String query = "SELECT " + columns +
					" FROM tbl_parameters WHERE scope = 'IMPACT_METHOD'";

			List<String> inserts = new ArrayList<>();
			NativeSql.on(db).query(query, r -> {
				long methodID = r.getLong(9);
				List<Long> impacts = methodImpacts.get(methodID);
				if (impacts == null || impacts.isEmpty())
					return true;
				for (long impact : impacts) {
					String insert = "INSERT INTO tbl_parameters ("
							+ columns + ")" + "VALUES (";
					insert += nextID.incrementAndGet() + ", "; // ID
					insert += "'" + UUID.randomUUID().toString() + "', "; // refID
					insert += _string(r, 3) + ", "; // name
					insert += _string(r, 4) + ", "; // description
					insert += "1, "; // version
					insert += new Date().getTime() + ", "; // last change
					insert += "NULL, "; // category
					insert += r.getInt(8) + ", "; // is input
					insert += impact + ", "; // owner
					insert += "'IMPACT_CATEGORY', "; // scope
					insert += r.getDouble(11) + ", "; // value
					insert += _string(r, 12) + ", "; // formula
					insert += _string(r, 13) + ", "; // external source
					insert += _string(r, 14) + ", "; // source type
					insert += _int(r, 15) + ", "; // distribution type
					insert += _double(r, 16) + ", "; // param1
					insert += _string(r, 17) + ", "; // param1 formula
					insert += _double(r, 18) + ", "; // param2
					insert += _string(r, 19) + ", "; // param2 formula
					insert += _double(r, 20) + ", "; // param3
					insert += _string(r, 21) + ")"; // param3 formula
					inserts.add(insert);
				}
				return true;
			});

			// delete the old parameters and insert the new ones
			NativeSql.on(db).runUpdate("DELETE FROM tbl_parameters " +
					"WHERE scope = 'IMPACT_METHOD'");
			if (inserts.isEmpty())
				return;
			NativeSql.on(db).batchUpdate(inserts);

		} catch (SQLException e) {
			throw new RuntimeException("failed to upgrade LCIA parameters", e);
		} finally {
			DbUtil.setLastID(db, nextID.get() + 5L);
		}
	}

	private String _string(ResultSet r, int col) {
		try {
			String s = r.getString(col);
			if (s == null)
				return "NULL";
			return "'" + s.replace("'", "''") + "'";
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private String _int(ResultSet r, int col) {
		try {
			int v = r.getInt(col);
			if (r.wasNull())
				return "NULL";
			return Integer.toString(v);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private String _double(ResultSet r, int col) {
		try {
			double v = r.getDouble(col);
			if (r.wasNull())
				return "NULL";
			return Double.toString(v);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
