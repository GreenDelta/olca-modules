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

import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.hash.TLongHashSet;

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

		// add new library table
		u.createTable("tbl_libraries",
				"CREATE TABLE tbl_libraries (" +
						" id VARCHAR(255)," +
						" PRIMARY KEY (id))");

		// add tags and library fields
		String[] tables = {
				"tbl_actors",
				"tbl_categories",
				"tbl_currencies",
				"tbl_dq_systems",
				"tbl_flows",
				"tbl_flow_properties",
				"tbl_impact_categories",
				"tbl_impact_methods",
				"tbl_locations",
				"tbl_parameters",
				"tbl_processes",
				"tbl_product_systems",
				"tbl_projects",
				"tbl_social_indicators",
				"tbl_sources",
				"tbl_unit_groups",
		};
		for (var table : tables) {
			u.createColumn(table, "tags VARCHAR(255)");
			u.createColumn(table, "library VARCHAR(255)");
		}

		// new regionalization features
		u.createColumn("tbl_exchanges", "f_location BIGINT");
		u.createColumn("tbl_impact_factors", "f_location BIGINT");
		u.createColumn("tbl_locations", "geodata BLOB(32 M)");

		// dynamic allocation factors
		u.createColumn("tbl_allocation_factors", "formula VARCHAR(1000)");

		addParameterSets(u);
		addStandaloneImpactCategories(db);
	}

	private void addParameterSets(DbUtil u) {
		if (u.tableExists("tbl_parameter_redef_sets"))
			return;

		u.createTable("tbl_parameter_redef_sets",
				"CREATE TABLE tbl_parameter_redef_sets ("
						+ " id BIGINT NOT NULL,"
						+ " name VARCHAR(2048),"
						+ " description CLOB(64 K),"
						+ " is_baseline SMALLINT default 0,"
						+ " f_product_system BIGINT)");
		u.createColumn("tbl_parameter_redefs", "description CLOB(64 K)");


		// move parameter redefinitions that were attached
		// to a product system into a baseline parameter set
		// of that product system
		AtomicLong seq = new AtomicLong(u.getLastID() + 5);
		try {

			// 1) collect the product system IDs
			TLongHashSet systemIDs = new TLongHashSet();
			String sql = "select id from tbl_product_systems";
			NativeSql.on(u.db).query(sql, r -> {
				systemIDs.add(r.getLong(1));
				return true;
			});

			// 2) move possible parameter redefinitions into parameter sets
			var systemSets = new TLongLongHashMap();
			sql = "select f_owner from tbl_parameter_redefs";
			NativeSql.on(u.db).updateRows(sql, rs -> {
				long owner = rs.getLong(1);
				if (!systemIDs.contains(owner))
					return true;
				long paramSet = systemSets.get(owner);
				if (paramSet == 0) {
					paramSet = seq.incrementAndGet();
					systemSets.put(owner, paramSet);
				}
				rs.updateLong(1, paramSet);
				rs.updateRow();
				return true;
			});

			// 3) create the allocated parameter sets
			TLongLongIterator it = systemSets.iterator();
			while (it.hasNext()) {
				it.advance();
				long systemID = it.key();
				long setID = it.value();
				String stmt = "insert into tbl_parameter_redef_sets "
						+ "(id, name, is_baseline, f_product_system) "
						+ "values (" + setID + ", 'Baseline', 1, "
						+ systemID + ")";
				NativeSql.on(u.db).runUpdate(stmt);
			}

			// finally update the ID sequence
			u.setLastID(seq.get() + 1);

		} catch (Exception e) {
			throw new RuntimeException("failed create parameter sets", e);
		}
	}

	private void addStandaloneImpactCategories(IDatabase db) {
		DbUtil u = new DbUtil(db);
		// make LCIA categories stand-alone entities
		u.createColumn("tbl_impact_categories", "f_category BIGINT");

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
		DbUtil u = new DbUtil(db);
		AtomicLong nextID = new AtomicLong(u.getLastID() + 5L);
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
			u.setLastID(nextID.get() + 5L);
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
